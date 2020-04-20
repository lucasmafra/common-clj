(ns common-clj.kafka.producer.config-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.producer.config :as nut])
  (:import org.apache.kafka.clients.producer.ProducerConfig))

(deftest producer-config
  (testing "kafka brokers"
    (is (= "localhost:9092,localhost:9091"
           (get-in
            (nut/producer-config {:kafka/brokers ["localhost:9092" "localhost:9091"]})
            [ProducerConfig/BOOTSTRAP_SERVERS_CONFIG])))))
