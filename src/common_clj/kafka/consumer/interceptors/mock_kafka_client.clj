(ns common-clj.kafka.consumer.interceptors.mock-kafka-client
  (:require [io.pedestal.interceptor :as interceptor])
  (:import [org.apache.kafka.clients.consumer MockConsumer OffsetResetStrategy]))

(def mock-kafka-client
  (interceptor/interceptor
   {:name  ::mock-kafka-client
    :enter (fn [context]
             (assoc context :kafka-client (new MockConsumer OffsetResetStrategy/LATEST)))}))
