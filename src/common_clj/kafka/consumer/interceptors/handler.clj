(ns common-clj.kafka.consumer.interceptors.handler
  (:require [common-clj.kafka.consumer.interceptors.helpers :refer [match-topic?]]
            [io.pedestal.interceptor :as interceptor]))

(def handler
  (interceptor/interceptor
   {:name  ::handler
    :enter (fn [{:keys [message record topics components]}]
             (let [topic     (ffirst (filter (match-topic? record) topics))
                   handle-fn (:handler (topics topic))]
               (handle-fn message components)))}))
