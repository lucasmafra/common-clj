(ns common-clj.components.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.http-server.protocol :refer [HttpServer]
             :as http-server.protocol]
            [common-clj.schemata.http :as schemata.http]
            [io.pedestal.http :as http]
            [schema.core :as s]))

(defn wrap-handler [handler components]
  (fn [request]
    (handler request components)))

(s/defn routes->pedestal [routes :- schemata.http/Routes components]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path method (wrap-handler handler components) :route-name route-name]))
   routes))

(defonce ^:private server (atom nil))

(s/defrecord HttpServerImpl [routes]
  component/Lifecycle
  (start [component]
    (let [pedestal-routes (routes->pedestal routes component)
          service (http-server.protocol/create-server component)]
      (http/start service)
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
