(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [set-init]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-server.http-server :as hs]
            [common-clj.http-server.interceptors.helpers :refer [ok]]
            [common-clj.kafka.consumer.consumer :as kafka-consumer]
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

(def producer-topics
  {:topic/a {:schema {:message s/Str}}})

(def consumer-topics
  {:topic/a
   {:topic  "TOPIC_A"
    :schema {:message s/Str}
    :handler (fn [message _]
               (println message))}})

(def dev-system
  (component/system-map
   :config      (imc/new-config config :dev)

   :http-server (component/using
                 (hs/new-http-server routes)
                 [:config])

   :producer (component/using
              (kafka-producer/new-producer producer-topics)
              [:config])

   :consumer (component/using
              (kafka-consumer/new-consumer consumer-topics)
              [:config :producer])))

(set-init (constantly dev-system))
