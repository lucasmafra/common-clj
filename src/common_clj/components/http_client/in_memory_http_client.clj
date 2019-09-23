(ns common-clj.components.http-client.in-memory-http-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.http-client.protocol :as hc-pro]
            [common-clj.schemata.http-client :as s-hc]
            [schema.core :as s]))

(s/defrecord InMemoryHttpClient [endpoints :- s-hc/Endpoints]
  component/Lifecycle
  (start [component])

  (stop [component])

  hc-pro/HttpClient
  (request [component endpoint options]))
