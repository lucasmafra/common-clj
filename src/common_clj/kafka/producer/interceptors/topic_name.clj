(ns common-clj.kafka.producer.interceptors.topic-name
  (:require [clojure.string :as str]
            [io.pedestal.interceptor :as interceptor]))

(defn ->kafka-topic [topic]
  (-> topic
      str
      str/upper-case
      (str/replace #":" "") ; removes :
      (str/replace \- \_) ; dash -> underscore
      (str/replace \/ \_) ; qualified keyword -> underscore      
      ))

(def topic-name
  (interceptor/interceptor
   {:name ::topic-name
    :enter (fn [{:keys [topic] :as context}]
             (assoc context :kafka-topic (->kafka-topic topic)))}))
