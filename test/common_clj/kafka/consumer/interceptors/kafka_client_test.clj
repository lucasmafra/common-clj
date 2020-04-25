(ns common-clj.kafka.consumer.interceptors.kafka-client-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.kafka-client :as nut]
            [io.pedestal.interceptor.chain :as chain])
  (:import [org.apache.kafka.clients.consumer ConsumerConfig KafkaConsumer]))

(def config
  {:kafka/brokers ["localhost:9092" "localhost:9091"]
   :app/name      :my-app})

(def context
  {:config config})

(deftest consumer-config
  (testing "kafka brokers"
    (is (= "localhost:9092,localhost:9091"
           (get-in
            (nut/consumer-config config)
            [ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG]))))
  
  (testing "consumer group"
    (is (= "my-app"
           (get-in
            (nut/consumer-config config)
            [ConsumerConfig/GROUP_ID_CONFIG])))))

(deftest kafka-client
  (testing "instantiates new kafka client"
    (let [{:keys [kafka-client]} (chain/execute context [nut/kafka-client])]
      (is (instance? KafkaConsumer kafka-client))
      (.close kafka-client))))
