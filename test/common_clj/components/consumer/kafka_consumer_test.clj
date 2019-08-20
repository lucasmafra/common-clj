(ns common-clj.components.consumer.kafka-consumer-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [common-clj.components.consumer.kafka-consumer :as consumer]
            [com.stuartsierra.component :as component]))

(def consumer-topics
  {:topic-a
   {:handler identity
    :schema s/Any}})

(def dummy-config
  (in-memory-config/new-config
   {:app-name :dummy-app
    :kafka-server  "localhost:9092"}))

(s/with-fn-validation
  (future-fact "it sets consumer group based on app name from config")
  (future-fact "it gets kafka server url from config")
  (future-fact "when a message arrives it executes handler of the corresponding topic"))


