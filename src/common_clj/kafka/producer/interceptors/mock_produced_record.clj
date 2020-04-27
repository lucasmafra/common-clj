(ns common-clj.kafka.producer.interceptors.mock-produced-record
  (:require [io.pedestal.interceptor :as interceptor]))

(def mock-produced-record
  (interceptor/interceptor
   {:name  ::mock-produced-record
    :leave (fn [{:keys [record produced-records] :as context}]
             (swap! produced-records update :records conj record)
             context)}))
