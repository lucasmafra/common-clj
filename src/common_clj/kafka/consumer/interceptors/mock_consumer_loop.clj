(ns common-clj.kafka.consumer.interceptors.mock-consumer-loop
  (:require [io.pedestal.interceptor :as interceptor]))

(def mock-consumer-loop
  (interceptor/interceptor
   {:name  ::mock-consumer-loop
    :enter (fn [context])}))
