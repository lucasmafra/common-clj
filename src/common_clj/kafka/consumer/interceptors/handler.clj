(ns common-clj.kafka.consumer.interceptors.handler
  (:require [io.pedestal.interceptor :as interceptor]))

(defn match-topic? [record]
  (fn [[_ {:keys [topic]}]] (= topic (.topic record))))

(def handler
  (interceptor/interceptor
   {:name  ::handler
    :enter (fn [{:keys [message record topics components]}]
             (let [topic     (ffirst (filter (match-topic? record) topics))
                   handle-fn (:handler (topics topic))]
               (handle-fn #nu/tap message components)))}))
