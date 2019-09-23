(ns common-clj.components.http-server.in-memory-http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.http-server.protocol
             :as
             http-server.protocol
             :refer
             [HttpServer]]
            [common-clj.schemata.http-server :as schemata.http]
            [io.pedestal.http :as http]
            [schema.core :as s]))

(s/defn routes->pedestal [routes :- schemata.http/Routes]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path method handler :route-name route-name]))
   routes))

(s/defrecord InMemoryHttpServer [routes]
  component/Lifecycle
  (start [component]
    (let [server (atom (http-server.protocol/create-server component))]
      (assoc component :server server)))

  (stop [component]
    (assoc component :server nil))

  HttpServer
  (create-server [component]
    (http/create-server
     {::http/routes (routes->pedestal routes)
      ::http/type   :jetty
      ::http/port   8080
      ::http/join?  false})))

(s/defn new-http-server [routes :- schemata.http/Routes]
  (map->InMemoryHttpServer {:routes routes}))
