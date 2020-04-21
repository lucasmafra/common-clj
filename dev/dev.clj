(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [set-init]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-server.http-server :as hs]
            [common-clj.http-server.interceptors.helpers :refer [ok]]
            [common-clj.kafka.producer.producer :as kafka-producer]
            [schema.core :as s]))

(def config
  {:app/name         :common-clj
   :http-server/port 9000
   :kafka/brokers    ["localhost:9092"]})

(def routes
  {:route/hello
   {:path            "/"
    :method          :get
    :response-schema s/Any
    :handler         (constantly (ok {:message "Hello, World!"}))}})

(def topics
  {:topic/a {:schema {:message s/Str}}})

(def dev-system
  (component/system-map
   :config      (imc/new-config config :dev)

   :http-server (component/using
                 (hs/new-http-server routes)
                 [:config])

   :producer (component/using
              (kafka-producer/new-producer topics)
              [:config])))

(set-init (constantly dev-system))
