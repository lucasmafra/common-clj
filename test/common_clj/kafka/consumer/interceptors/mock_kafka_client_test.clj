(ns common-clj.kafka.consumer.interceptors.mock-kafka-client-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.kafka-client :as i-kafka-client]
            [common-clj.kafka.consumer.interceptors.mock-kafka-client :as nut]
            [io.pedestal.interceptor.chain :as chain])
  (:import org.apache.kafka.clients.consumer.MockConsumer))

(deftest mock-kafka-client
  (testing "instantiates MockConsumer and removes kafka-client from queue"
    (let [{:keys [kafka-client]} (chain/execute {} [nut/mock-kafka-client i-kafka-client/kafka-client])]
      (is (instance? MockConsumer kafka-client)))))
