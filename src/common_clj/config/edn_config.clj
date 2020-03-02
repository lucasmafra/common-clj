(ns common-clj.config.edn-config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as config-protocol]
            [common-clj.config.schemata :as s-config]
            [schema.core :as s]))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))
    (catch RuntimeException e
      (throw
       (ex-info
        "Error parsing edn file. Make sure you have your app config at 'resources/app.edn'"
        {:error-message (.getMessage e)
         :file source})))))

(defonce loaded-config (atom nil))

(s/defrecord EdnConfig [env :- s-config/Env]
  component/Lifecycle
  (start [component]
    (let [config (->> "app.edn" io/resource load-edn)]
      (s/validate s-config/AppConfig config)
      (reset! loaded-config config)
      component))

  (stop [component]
    (reset! loaded-config nil)
    component)

  config-protocol/Config
  (get-config [_]
    @loaded-config)

  (get-env [component]
    env)

  (assoc-in! [component ks v]
    (swap! loaded-config assoc-in ks v)))

(s/defn new-config
  ([] (new-config :prod))
  ([env :- s-config/Env]
   (map->EdnConfig {:env env})))
