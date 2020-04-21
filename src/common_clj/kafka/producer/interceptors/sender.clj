(ns common-clj.kafka.producer.interceptors.sender
  (:require [io.pedestal.interceptor :as interceptor])
  (:import org.apache.kafka.clients.producer.ProducerRecord))

(defn- ->record [topic message]
  (ProducerRecord. topic message))

(def sender
  (interceptor/interceptor
   {:name ::sender
    :enter (fn [{:keys [producer kafka-topic message] :as context}]
             (.send producer (->record kafka-topic message))
             context)}))
