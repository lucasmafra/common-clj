(ns common-clj.kafka.consumer.interceptors.consumer-loop
  (:require [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]))

(def consumer-loop
  (interceptor/interceptor
   {:name  ::consumer-loop
    :enter (fn [{:keys [kafka-client consume-interceptors] :as context}]
             (future
               (try
                 (while true
                   (let [records (seq (.poll kafka-client Long/MAX_VALUE))]
                     (doseq [record records]
                       (chain/execute (merge context {:record record}) consume-interceptors))))
                 (catch Exception _
                   (.close kafka-client))))
             context)}))
