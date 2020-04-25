(ns common-clj.kafka.consumer.interceptors.subscriber-test
  (:require [clojure.test :refer [deftest is testing]]
            [common-clj.kafka.consumer.interceptors.subscriber :as nut]
            [io.pedestal.interceptor.chain :as chain])
  (:import [org.apache.kafka.clients.consumer MockConsumer OffsetResetStrategy]))

(def topics
  {:topic-a {:topic "TOPIC_A"}
   :topic-b {:topic "TOPIC_B"}})

(def context
  {:topics       topics
   :kafka-client (new MockConsumer OffsetResetStrategy/LATEST)})

(deftest subscriber
  (testing "subscribes to given topics"
    (let [{:keys [kafka-client]} (chain/execute context [nut/subscriber])]
      (is (= #{"TOPIC_A" "TOPIC_B"}
             (.subscription kafka-client))))))
