(ns common-clj.kafka.consumer.interceptors.mock-kafka-client
  (:require [io.pedestal.interceptor :as interceptor])
  (:import [org.apache.kafka.clients.consumer MockConsumer OffsetResetStrategy]))

(def i-kafka-client
  :common-clj.kafka.consumer.interceptors.kafka-client/kafka-client)

(def mock-kafka-client
  (interceptor/interceptor
   {:name  ::mock-kafka-client
    :enter (fn [{:keys [:io.pedestal.interceptor.chain/queue] :as context}]
             (let [modified-queue (remove #(= i-kafka-client (:name %)) queue)]
               (-> context
                   (assoc :kafka-client (new MockConsumer OffsetResetStrategy/EARLIEST))
                   (assoc :io.pedestal.interceptor.chain/queue modified-queue))))}))
