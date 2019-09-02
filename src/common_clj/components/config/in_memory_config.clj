(ns common-clj.components.config.in-memory-config
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :refer [Config]]
            [common-clj.schemata.config :as schemata.config]
            [schema.core :as s]))

(s/defrecord InMemoryConfig [config :- schemata.config/AppConfig
                             env :- schemata.config/Env]
  component/Lifecycle
  (start [component]
    (s/validate schemata.config/AppConfig config)
    component)

  (stop [component]
    component)

  Config
  (get-config [component]
    config)

  (get-env [component]
    env))

(s/defn new-config
  ([config :- schemata.config/AppConfig]
   (new-config config :prod))
  ([config :- schemata.config/AppConfig env :- schemata.config/Env]
   (map->InMemoryConfig {:config config :env env})))
