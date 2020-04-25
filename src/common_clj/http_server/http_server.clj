(ns common-clj.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as config-protocol]
            [common-clj.http-server.helpers :refer [->pedestal-routes]]
            [common-clj.http-server.interceptors.context-initializer :as i-ctx]
            [common-clj.http-server.schemata :as s-hs]
            [io.pedestal.http :as http]
            [schema.core :as s]))

(defn- assert-dependencies [{:keys [config]}]
  (assert config "Missing dependency: "))

(defn- assert-config [config-map]
  (s/validate s-hs/HttpServerConfig config-map))

(defn- build-base-interceptors [{:keys [env] :as service-map} component]
  (cond-> service-map
    (not= env :prod) (assoc ::http/allowed-origins {:creds true :allowed-origins (constantly true)})
    true             http/default-interceptors
    (not= env :prod) http/dev-interceptors
    true             (update ::http/interceptors #(cons (i-ctx/context-initializer component) %))))

(defn- build-service-map [{:keys [config overrides] :as component}]
  (let [env                                       (config-protocol/get-env config)
        {:keys [http-server/port] :as config-map} (config-protocol/get-config config)
        {:keys [service-map]}                     overrides]
    (assert-config config-map)
    (build-base-interceptors
     (merge
      {::http/type   :jetty
       ::http/port   port
       ::http/host   "0.0.0.0"
       ::http/join?  false
       ::http/routes (->pedestal-routes component)
       :env          env}
      service-map)
     component)))

(defn- start-server [{:keys [env] :as service-map}]
  (if (= env :test)
    service-map
    (http/start service-map)))

(defn- stop-server [{:keys [service-map]}]
  (when service-map
    (http/stop service-map)))

(defn- assoc-components [component]
  (assoc component :components component))

(s/defrecord HttpServerImpl
             [routes :- s-hs/Routes
              overrides :- s-hs/Overrides]
  component/Lifecycle
  (start [component]
    (assert-dependencies component)
    (->> component
         assoc-components
         (merge {:routes routes :overrides overrides})
         build-service-map
         http/create-server
         start-server
         (assoc component :service-map)))

  (stop [component]
    (stop-server component)
    (dissoc component :service-map)))

(s/defn new-http-server
  ([routes :- s-hs/Routes]
   (new-http-server routes {}))
  ([routes :- s-hs/Routes
    overrides :- s-hs/Overrides]
   (map->HttpServerImpl {:routes routes :overrides overrides})))
