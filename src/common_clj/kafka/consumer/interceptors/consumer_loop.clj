(ns common-clj.kafka.consumer.interceptors.consumer-loop
  (:require [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]))

(defn clear-pedestal-keys [context]
  (apply dissoc context [:io.pedestal.interceptor.chain/execution-id
                         :io.pedestal.interceptor.chain/queue
                         :io.pedestal.interceptor.chain/stack]))

(def consumer-loop
  (interceptor/interceptor
   {:name  ::consumer-loop
    :enter
    (fn [{:keys [kafka-client consume-interceptors] :as context}]
      (future
        (try
          (while true
            (let [records (seq (.poll kafka-client Long/MAX_VALUE))]
              (doseq [record records]
                (let [initial-context (-> context (assoc :record record) clear-pedestal-keys)]
                  (chain/execute initial-context consume-interceptors)))))
          (catch Exception _
            (.close kafka-client))))
      context)}))
