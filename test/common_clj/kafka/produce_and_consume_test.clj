(ns common-clj.kafka.produce-and-consume-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.kafka.consumer.consumer :as kafka-consumer]
            [common-clj.kafka.producer.producer :as kafka-producer]
            [common-clj.kafka.producer.protocol :as producer-pro]
            [common-clj.key-value-store.in-memory-key-value-store :as in-memory-kvs]
            [common-clj.key-value-store.protocol :as kvs-pro]
            [schema.core :as s]))

(def config
  {:app/name      :my-app
   :kafka/brokers ["localhost:9092"]})

(def SchemaA {:a s/Str})

(def producer-topics
  {:topic/a
   {:topic  "TOPIC_A"
    :schema SchemaA}})

(def consumer-topics
  {:topic/a
   {:topic "TOPIC_A"
    :schema SchemaA
    :handler (fn [message {:keys [db]}]
               (kvs-pro/store! db :topic/a message))}})

(def system
  (component/system-map
   :config (imc/new-config config :test)
   
   :producer (component/using
              (kafka-producer/new-producer producer-topics)
              [:config])

   :db       (in-memory-kvs/new-key-value-store)

   :consumer (component/using
              (kafka-consumer/new-consumer consumer-topics)
              [:config :producer :db])))

(deftest produce-and-consume
  (testing "produces and consumes message"
    (let [{:keys [producer db]} (component/start system)]
      (producer-pro/produce! producer :topic/a {:a "hello"})

      (is (= {:a "hello"}
             (kvs-pro/fetch db :topic/a))))))
