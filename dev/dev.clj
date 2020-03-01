(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [reset set-init]]
            [common-clj.components.config.in-memory-config :as imc]
            [common-clj.http-server.http-server :as hs]
            [schema.core :as s]))

(def config
  {:app-name  :common-clj
   :http-port 9000})

(def routes
  {:route/hello
   {:path            "/"
    :method          :get
    :response-schema s/Any
    :handler         (fn [_ _] (/ 1 0))}})

(def dev-system
  (component/system-map
   :config      (imc/new-config config :dev)

   :http-server (component/using
                 (hs/new-http-server routes)
                 [:config])))

(set-init (constantly dev-system))
