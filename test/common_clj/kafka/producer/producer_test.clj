(ns common-clj.kafka.producer.producer-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.clojure-test-helpers.producer :refer [get-produced-messages]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.kafka.producer.producer :as nut]
            [common-clj.kafka.producer.protocol :as producer-pro]
            [schema.core :as s])
  (:import org.apache.kafka.clients.producer.MockProducer))

(def SchemaA {:a s/Str})

(def producer-settings {:topic-a {:schema SchemaA}})

(def init-mock-kafka-producer (constantly (MockProducer.)))

(def config {:app/name      :my-app
             :kafka/brokers ["localhost:9092"]})

(def system
  (component/system-map
   :config (imc/new-config config :test)
   :producer (component/using
              (nut/new-producer producer-settings)
              [:config])))

(defn- start-producer []
  (with-redefs [nut/init-kafka-producer init-mock-kafka-producer]
    (:producer (component/start system))))

(deftest produce!
  (testing "produces a message to kafka topic"
    (let [producer (start-producer)]
      (producer-pro/produce! producer :topic-a {:a "hello"})

      (is (= ["{\"a\":\"hello\"}"]
             (get-produced-messages "TOPIC_A" producer)))))

  (testing "closes producer on component/stop"
    (let [producer (start-producer)]
      (is (= true
             (-> producer
                 component/stop
                 :kafka-producer
                 .closed))))))
