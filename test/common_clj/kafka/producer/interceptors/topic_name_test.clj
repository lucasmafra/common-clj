(ns common-clj.kafka.producer.interceptors.topic-name-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.producer.interceptors.topic-name :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(deftest topic-name
  (testing "converts topic name from keyword to UPPER_CASE string"
    (let [initial-context {:topic :my-topic}]
      (is (= "MY_TOPIC"
             (get-in
              (chain/execute initial-context [nut/topic-name])
              [:kafka-topic])))))

  (testing "when namespaced keyword adds prefix with _"
    (let [initial-context {:topic :my-very/first-topic}]
      (is (= "MY_VERY_FIRST_TOPIC"
             (get-in
              (chain/execute initial-context [nut/topic-name])
              [:kafka-topic]))))))
