(ns common-clj.kafka.consumer.interceptors.mock-consumer-loop-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.mock-consumer-loop :as nut]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [common-clj.kafka.consumer.interceptors.consumer-loop :as i-loop])
  (:import [org.apache.kafka.clients.consumer ConsumerRecord MockConsumer OffsetResetStrategy]))

(def dummy-interceptor
  (interceptor/interceptor
   {:name ::dummy
    :enter (fn [{:keys [record consumed-messages] :as context}]
             (swap! consumed-messages update (.topic record) conj (.value record))
             context)}))

(defn context []
  {:kafka-client         (doto (new MockConsumer OffsetResetStrategy/EARLIEST)
                           (.subscribe ["TOPIC_A"]))
   :consume-interceptors [dummy-interceptor]
   :consumed-messages    (atom {"TOPIC_A" []})
   :producer             {:produced-records (atom {:records []})}})

(def hello (new ConsumerRecord "TOPIC_A" 0 0 0 "hello"))
(def bye (new ConsumerRecord "TOPIC_A" 0 1 0 "bye"))

(defn produce-record! [record]
  (interceptor/interceptor
   {:name ::produce-record
    :enter (fn [{{:keys [produced-records]} :producer :as context}]
             (swap! produced-records update :records conj record)
             context)}))

(def fake-consumer-loop
  (interceptor/interceptor
   {:name :common-clj.kafka.consumer.interceptors.consumer-loop/consumer-loop
    :enter (fn [{:keys [consumed-messages] :as context}]
             (swap! consumed-messages update "TOPIC_A" conj "message")
             context)}))

(deftest mock-consumer-loop
  (testing "polls records from produced-messages and consumes them"
    (let [{:keys [consumed-messages]} (chain/execute (context) [nut/mock-consumer-loop
                                                                (produce-record! hello)
                                                                (produce-record! bye)])]
      (is (= ["hello" "bye"]
             (-> consumed-messages deref (get "TOPIC_A"))))))
  (testing "removes consumer loop from the interceptor queue"
    (let [{:keys [consumed-messages]} (chain/execute (context) [nut/mock-consumer-loop
                                                                fake-consumer-loop])]
      (is (-> consumed-messages deref (get "TOPIC_A") empty?)))))
