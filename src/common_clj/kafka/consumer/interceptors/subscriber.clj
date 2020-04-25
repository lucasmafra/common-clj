(ns common-clj.kafka.consumer.interceptors.subscriber
  (:require [io.pedestal.interceptor :as interceptor]))

(def subscriber
  (interceptor/interceptor
   {:name  ::subscriber
    :enter (fn [{:keys [topics kafka-client] :as context}]
             (.subscribe kafka-client (map (fn [[_ {:keys [topic]}]] topic) topics))
             context)}))
