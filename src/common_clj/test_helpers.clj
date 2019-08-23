(ns common-clj.test-helpers
  (:require [cheshire.core :refer [generate-string]]
            [com.stuartsierra.component :as component]
            [common-clj.components.consumer.protocol :as consumer.protocol]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.components.producer.protocol :as producer.protocol]
            [common-clj.lib.kafka :refer [kafka-topic->topic]]
            [selvage.midje.flow :refer [*world* flow]])
  (:import (org.apache.kafka.clients.consumer ConsumerRecord KafkaConsumer
                                              MockConsumer OffsetResetStrategy)
           (org.apache.kafka.clients.producer MockProducer)
           (org.apache.kafka.common TopicPartition)))

(defn init!
  "setup components and store them in the world"
  [system-map world]
  (let [system (component/start system-map)]
    (assoc world :system system)))

(defn message-arrived!
  [topic message world]
  (let [consumer (-> world :system :consumer)]
    (consumer.protocol/consume! consumer topic message)
    world))

(defn try-consume!
  "Try to consume message and, if any error is thrown while consuming,
  add the error to world in path [:consumption-errors <topic>]"
  [topic message world]
  (try
    (message-arrived! topic message world)
    (catch Exception e
      (update-in world [:consumption-errors topic] (partial cons e)))))

(defn schema-error? [exception-info]
  (-> exception-info
      ex-data
      :type
      (= :schema.core/error)))

(defn kafka-message-arrived!
  [topic message world]
  (let [kafka-client (-> world :system :consumer :kafka-client)]
    (.rebalance kafka-client [(TopicPartition. topic 0)])
    (.updateBeginningOffsets kafka-client {(TopicPartition. topic 0) 0})
    (.updateEndOffsets kafka-client {(TopicPartition. topic 0) 1})
    (.addRecord kafka-client (ConsumerRecord. topic 0 0 "key" (generate-string message))))
  world)

(defn kafka-try-consume!
  [topic message world]
  (let [logger (-> world :system :logger)
        error-handler (reify Thread$UncaughtExceptionHandler    
                        (uncaughtException [_ _ e] 
                          (logger.protocol/log! logger topic e)))
        consumer-thread (-> world :system :consumer :consumer-thread)]
    (.setUncaughtExceptionHandler consumer-thread error-handler)
    (kafka-message-arrived! topic message world)))

(defn mock-kafka-client [& args] (MockConsumer. OffsetResetStrategy/EARLIEST))

(defn produce! [topic message world]
  (let [producer (-> world :system :producer)]
    (producer.protocol/produce! producer topic message)
    world))

(defn try-produce!
  [topic message world]
  (try
    (produce! topic message world)
    (catch Exception e
      (update-in world [:producer-error topic] #(conj % e)))))

(defn check-produced-messages
  [topic]
  (or (-> *world* :system :producer :messages deref topic)
      []))

(defn check-produced-errors
  [topic]
  (or (-> *world* :producer-error topic)
      []))

(def exception? (partial instance? Exception))

(defn kafka-produce!
  [topic message world]
  (let [producer (-> world :system :producer)]
    (producer.protocol/produce! producer topic message)
    world))

(defn kafka-try-produce!
  [topic message world]
  (try
    (produce! topic message world)
    (catch Exception e
      (update-in world [:producer-error topic] #(conj % e)))))

(defn check-kafka-produced-messages [topic]
  (let [kafka-client (-> *world* :system :producer :kafka-client)]
    (->> kafka-client
         .history
         vec
         (map (fn [record] {:kafka-topic (.topic record)
                            :value       (.value record)}))
         (filter (fn [{:keys [kafka-topic]}] (= topic kafka-topic)))
         (map :value))))

(defn check-kafka-produced-errors [topic]
  (or (-> *world* :producer-error topic)
      []))

(defn mock-kafka-producer [& args] (MockProducer.))