(ns common-clj.components.config.in-memory-config
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :refer [Config]]
            [common-clj.schemata.config :as schemata.config]
            [schema.core :as s]))

(s/defrecord InMemoryConfig [config :- schemata.config/AppConfig]
  component/Lifecycle
  (start [component]
    (assoc component :config config))

  Config
  (get-config [{:keys [config]}]
    config))

(s/defn new-config [config :- schemata.config/AppConfig]
  (map->InMemoryConfig {:config config}))
