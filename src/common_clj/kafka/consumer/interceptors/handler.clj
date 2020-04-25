(ns common-clj.kafka.consumer.interceptors.handler
  (:require [io.pedestal.interceptor :as interceptor]))

(defn match-topic? [record]
  (fn [[_ {:keys [topic]}]] (= topic (.topic record))))

(def handler
  (interceptor/interceptor
   {:name  ::handler
    :enter (fn [{:keys [record topics components]}]
             (let [topic     (ffirst (filter (match-topic? record) topics))
                   message   (.value record)
                   handle-fn (:handler (topics topic))]
               (handle-fn message components)))}))
