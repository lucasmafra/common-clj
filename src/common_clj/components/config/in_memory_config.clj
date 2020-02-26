(ns common-clj.components.config.in-memory-config
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :refer [Config]]
            [common-clj.schemata.config :as schemata.config]
            [schema.core :as s]))

(defonce loaded-config (atom nil))

(s/defrecord InMemoryConfig [config :- schemata.config/AppConfig
                             env :- schemata.config/Env]
  component/Lifecycle
  (start [component]
    (s/validate schemata.config/AppConfig config)
    (reset! loaded-config config)
    component)

  (stop [component]
    (reset! loaded-config nil)
    component)

  Config
  (get-config [component]
    @loaded-config)

  (get-env [component]
    env)

  (assoc-in! [component ks v]
    (swap! loaded-config assoc-in ks v))) 

(s/defn new-config
  ([config :- schemata.config/AppConfig]
   (new-config config :prod))
  ([config :- schemata.config/AppConfig env :- schemata.config/Env]
   (map->InMemoryConfig {:config config :env env})))
