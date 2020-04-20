(ns common-clj.kafka.producer.interceptors.sender-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.producer.interceptors.sender :as nut]
            [io.pedestal.interceptor.chain :as chain])
  (:import org.apache.kafka.clients.producer.MockProducer))

(deftest sender
  (testing "produces message to topic"
    (let [context {:producer    (MockProducer.)
                   :kafka-topic "MY_TOPIC"
                   :message     "hello"}]
      (chain/execute context [nut/sender])

      (is (= ["hello"]
             (->> context :producer .history (mapv #(.value %)))))

      (is (= ["MY_TOPIC"]
             (->> context :producer .history (mapv #(.topic %))))))))
