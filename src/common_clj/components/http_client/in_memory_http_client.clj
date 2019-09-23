(ns common-clj.components.http-client.in-memory-http-client
  (:require [schema.core :as s]
            [common-clj.schemata.http-client :as s-hc]
            [common-clj.components.http-client.protocol :as hc-pro]
            [com.stuartsierra.component :as component]))

(s/defrecord InMemoryHttpClient [endpoints :- s-hc/Endpoints]
  component/Lifecycle
  (start [component])

  (stop [component])

  hc-pro/HttpClient
  (request [component endpoint options]))
