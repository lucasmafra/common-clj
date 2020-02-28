(ns common-clj.components.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.http-server.protocol
             :as
             http-server.protocol
             :refer
             [HttpServer]]
            [common-clj.http-server.interceptors.body-coercer :as i-body-coercer]
            [common-clj.http-server.interceptors.content-type :as i-content-type]
            [common-clj.http-server.interceptors.context-initializer :as i-ctx]
            [common-clj.http-server.interceptors.error :as i-error]
            [common-clj.http-server.interceptors.json-serializer
             :as
             i-json-serializer]
            [common-clj.http-server.interceptors.path-params-coercer
             :as
             i-path-params-coercer]
            [common-clj.http-server.interceptors.query-params-coercer
             :as
             i-query-params-coercer]
            [common-clj.schemata.http-server :as schemata.http]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [schema.core :as s]))

(def default-handler-interceptors
  [;; error
   i-error/error
   
   ;; enter
   (body-params)
   i-body-coercer/body-coercer
   i-path-params-coercer/path-params-coercer
   i-query-params-coercer/query-params-coercer
   
   ;; leave
   i-json-serializer/json-serializer
   i-content-type/content-type])

(defn default-interceptors-fn [{:keys [env] :as service-map}]
  (cond-> service-map
    true             http/default-interceptors
    (not= env :prod) http/dev-interceptors))

(def base-service-map
  {::http/host            "0.0.0.0"
   ::http/type            :jetty
   ::http/join?           false})

(defn build-service-map [override-service-map http-port pedestal-routes env]
  (merge base-service-map
         {::http/port   http-port
          ::http/routes pedestal-routes
          :env          env}
         override-service-map))

(defn wrap-handler [handler components]
  (fn [request]
    (let [{:keys [status body]} (handler request components)]
      {:status status
       :body   body})))

(s/defn routes->pedestal
  [routes :- schemata.http/Routes
   overrides :- (s/maybe schemata.http/Overrides)
   env :- s/Keyword
   components]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path
       method
       (conj default-handler-interceptors (wrap-handler handler components))
       :route-name route-name]))
   routes))

(s/defrecord HttpServerImpl [routes overrides]
  component/Lifecycle
  (start [{:keys [config] :as components}]
    (when (nil? config)
      (throw "Missing dependency :config on http-server"))
    (let [env     (config.protocol/get-env config)
          service (http-server.protocol/create-server components)]
      (when (not= :test env)
        (http/start service))
      (-> components
          (assoc :service service))))

  (stop [{:keys [service] :as component}]
    (when service
      (http/stop service))
    (dissoc component :service))

  HttpServer
  (create-server [{:keys [config overrides] :as components}]
    (let [env                                   (config.protocol/get-env config)
          {:keys [http-port]}                   (config.protocol/get-config config)
          pedestal-routes                       (routes->pedestal routes overrides env components)
          {:keys [service-map interceptors-fn]} overrides
          http-port                             (or http-port 80)
          service-map                           (build-service-map service-map http-port pedestal-routes env)
          interceptors-fn                       (or interceptors-fn default-interceptors-fn)]
      (-> service-map
          interceptors-fn
          (update ::http/interceptors #(cons (i-ctx/context-initializer routes env overrides) %))
          (http/create-server)))))

(s/defn new-http-server
  ([routes :- schemata.http/Routes]
   (new-http-server routes {}))
  ([routes :- schemata.http/Routes
    overrides :- schemata.http/Overrides]
   (map->HttpServerImpl {:routes routes :overrides overrides})))
