(ns common-clj.components.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.coercion :refer [coerce]]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.http-server.protocol :refer [HttpServer]
             :as http-server.protocol]
            [common-clj.json :refer [json->string]]
            [common-clj.schemata.http :as schemata.http]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.error :as error-int]
            [schema.core :as s]))

(defn wrap-handler [handler components]
  (fn [request]
    (handler request components)))

(def content-type
  (interceptor
   {:name ::content-type
    :leave (fn [context]
             (assoc-in context
                       [:response :headers]
                       {"Content-Type" "application/json"}))}))
(defn body-coercer
  [routes]
  (interceptor
   {:name  ::json-coercer
    :enter (fn [{:keys [request route] :as context}]
             (let [{:keys [json-params]}    request
                   {:keys [route-name]}     route
                   {:keys [request-schema]} (route-name routes)
                   coerced-body             (when request-schema (coerce request-schema json-params))]
               (assoc-in context [:request :body] coerced-body)))
    :leave (fn [{:keys [response route] :as context}]
             (let [{:keys [body]}            response
                   {:keys [route-name]}      route
                   {:keys [response-schema]} (route-name routes)
                   coerced-body              (json->string body)]
               (s/validate response-schema body)
               (assoc-in context [:response :body] coerced-body)))}))

(def error-interceptor
  (error-int/error-dispatch [ctx ex]
     [{:type :schema-tools.coerce/error}]
     (assoc ctx :response {:status 400 :body (ex-data ex)})))

(defn interceptors [routes]
  [content-type
   error-interceptor
   (body-params)
   (body-coercer routes)])

(s/defn routes->pedestal [routes :- schemata.http/Routes components]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path
       method
       (conj (interceptors routes) (wrap-handler handler components))
       :route-name route-name]))
   routes))

(defonce ^:private server (atom nil))

(s/defrecord HttpServerImpl [routes]
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [pedestal-routes (routes->pedestal routes component)
          service (http-server.protocol/create-server component)
          env (config.protocol/get-env config)]
      (when (not (= :test env))
        (http/start service))
      (reset! server service)
      (-> component
          (assoc :service service)
          (assoc :pedestal-routes pedestal-routes))))

  (stop [component]
    (http/stop @server)
    component)

  HttpServer
  (create-server [{:keys [config] :as component}]
    (let [{:keys [http-port]} (config.protocol/get-config config)]
      (http/create-server
       {::http/routes (routes->pedestal routes component)
        ::http/type   :jetty
        ::http/port   http-port
        ::http/join?  false}))))

(s/defn new-http-server [routes :- schemata.http/Routes]
  (map->HttpServerImpl {:routes routes}))
