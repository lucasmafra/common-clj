(ns common-clj.kafka.consumer.interceptors.handler-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.handler :as nut]
            [common-clj.key-value-store.in-memory-key-value-store :as in-memory-kvs]
            [common-clj.key-value-store.protocol :as kvs-pro]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import org.apache.kafka.clients.consumer.ConsumerRecord))

(def hello (new ConsumerRecord "TOPIC_A" 0 0 0 #json {"a" "hello"}))

(defn ->context []
  {:message {:a "hello"}

   :record hello

   :topics
   {:topic/a
    {:topic   "TOPIC_A"
     :schema  {:a s/Str}
     :handler (fn [message {:keys [db]}]
                (kvs-pro/store! db :topic/a message))}}

   :components
   {:db (component/start (in-memory-kvs/new-key-value-store))}})

(deftest handler
  (testing "calls the handler fn according to record topic"
    (let [{{:keys [db]} :components :as context} (->context)]
      (chain/execute context [nut/handler])

      (is (= {:a "hello"}
             (kvs-pro/fetch db :topic/a))))))
