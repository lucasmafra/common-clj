(ns common-clj.components.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.http-server.protocol :refer [HttpServer]
             :as http-server.protocol]
            [common-clj.schemata.http :as schemata.http]
            [io.pedestal.http :as http]
            [schema.core :as s]))

(s/defn routes->pedestal [routes :- schemata.http/Routes]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path method handler :route-name route-name]))
   routes))

(defonce ^:private server (atom nil))

(s/defrecord HttpServerImpl [routes]
  component/Lifecycle
  (start [component]
    (->> component
        http-server.protocol/create-server
        http/start
        (reset! server))
    component)

  (stop [component]
    (http/stop @server)
    component)

  HttpServer
  (create-server [{:keys [config]}]
    (let [{:keys [http-port]} (config.protocol/get-config config)]
      (http/create-server
       {::http/routes (routes->pedestal routes)
        ::http/type   :jetty
        ::http/port   http-port
        ::http/join?  false}))))

(s/defn new-http-server [routes :- schemata.http/Routes]
  (map->HttpServerImpl {:routes routes}))
