(ns common-clj.kafka.producer.interceptors.sender
  (:require [io.pedestal.interceptor :as interceptor])
  (:import org.apache.kafka.clients.producer.ProducerRecord))

(defn- ->record [topic message]
  (ProducerRecord. topic message))

(def sender
  (interceptor/interceptor
   {:name  ::sender
    :enter (fn [{:keys [kafka-client message topics topic] :as context}]
             (let [kafka-topic (-> topics topic :topic)
                   record      (->record kafka-topic message)]
               (.send kafka-client record)
               (assoc context :record record)))}))
