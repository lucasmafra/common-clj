(ns common-clj.config.in-memory-config
  (:require [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as config-protocol]
            [common-clj.config.schemata :as s-config]
            [schema.core :as s]))

(defonce loaded-config (atom nil))

(s/defrecord InMemoryConfig [config :- s-config/AppConfig
                             env :- s-config/Env]
  component/Lifecycle
  (start [component]
    (s/validate s-config/AppConfig config)
    (reset! loaded-config config)
    component)

  (stop [component]
    (reset! loaded-config nil)
    component)

  config-protocol/Config
  (get-config [component]
    @loaded-config)

  (get-env [component]
    env)

  (assoc-in! [component ks v]
    (swap! loaded-config assoc-in ks v)))

(s/defn new-config
  ([config :- s-config/AppConfig]
   (new-config config :prod))
  ([config :- s-config/AppConfig env :- s-config/Env]
   (map->InMemoryConfig {:config config :env env})))
