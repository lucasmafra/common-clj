(ns common-clj.kafka.consumer.consumer-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.kafka.consumer.consumer :as nut]
            [common-clj.key-value-store.in-memory-key-value-store :as in-memory-kvs]
            [common-clj.key-value-store.protocol :as kvs-pro]
            [schema.core :as s]
            [common-clj.kafka.producer.producer :as kafka-producer]))

(def SchemaA {:a s/Str})
(def SchemaB {:b s/Str})
(def SchemaC {:c s/Str})

(def config {:app/name      :my-app
             :kafka/brokers ["localhost:9092"]})

(def topics
  {:topic/a
   {:topic   "TOPIC_A"
    :schema  SchemaA
    :handler (fn [message {:keys [key-value-store]}]
               (kvs-pro/store! key-value-store :topic/a message))}

   :topic/b
   {:topic   "TOPIC_B"
    :schema  SchemaB
    :handler (constantly nil)}

   :topic/c
   {:topic   "TOPIC_C"
    :schema  SchemaC
    :handler (constantly nil)}})

(def system
  (component/system-map
   :config          (imc/new-config config :test)
   :key-value-store (in-memory-kvs/new-key-value-store)
   :producer        (component/using
                     (kafka-producer/new-producer {})
                     [:config])
   :consumer        (component/using
                     (nut/new-consumer topics)
                     [:config :key-value-store :producer])))

(defn- start-consumer []
  (component/start system))

(defn- produce! [{:keys [kafka-consumer]} topic message])

(defn- fetch [key-value-store topic]
  (kvs-pro/fetch key-value-store topic))

(deftest consume!
  (testing "consumes a message from a kafka topic"
    (let [{:keys [consumer key-value-store]} (start-consumer)]

      (produce! consumer "TOPIC_A" #json {"a" "hello"})

      (is (= #json {"a" "hello"} (fetch key-value-store :topic/a)))
      (is (= nil (fetch key-value-store :topic/b)))
      (is (= nil (fetch key-value-store :topic/c)))

      (produce! consumer "TOPIC_B" #json {"b" "hello"})

      #_(is (= 1 (get-count key-value-store :topic/a)))
      #_(is (= 1 (get-count key-value-store :topic/b)))
      #_(is (= 0 (get-count key-value-store :topic/c)))

      #_(produce! consumer "TOPIC_A" #json {"a" "hello"})
      #_(produce! consumer "TOPIC_A" #json {"c" "hello"})

      #_(is (= 2 (get-count key-value-store :topic/a)))
      #_(is (= 1 (get-count key-value-store :topic/b)))
      #_(is (= 1 (get-count key-value-store :topic/c)))

      (component/stop consumer))))