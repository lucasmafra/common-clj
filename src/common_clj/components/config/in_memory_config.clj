(ns common-clj.components.config.in-memory-config
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :refer [Config]]
            [common-clj.schemata.config :as schemata.config]
            [schema.core :as s]))

(s/defrecord InMemoryConfig [config :- schemata.config/AppConfig]
  component/Lifecycle
  (start [component]
    (s/validate schemata.config/AppConfig config)
    component)

  (stop [component]
    component)

  Config
  (get-config [component]
    config))

(s/defn new-config [config :- schemata.config/AppConfig]
  (map->InMemoryConfig {:config config}))
