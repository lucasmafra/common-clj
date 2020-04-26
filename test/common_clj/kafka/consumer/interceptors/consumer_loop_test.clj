(ns common-clj.kafka.consumer.interceptors.consumer-loop-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.consumer-loop :as nut]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain])
  (:import [org.apache.kafka.clients.consumer ConsumerRecord MockConsumer OffsetResetStrategy]
           org.apache.kafka.common.TopicPartition))

(def partition-0 (new TopicPartition "TOPIC_A" 0))
(def first-message "hello")
(def second-message "bye")

(def dummy-interceptor
  (interceptor/interceptor
   {:name ::dummy
    :enter (fn [{:keys [record consumed-messages] :as context}]
             (swap! consumed-messages update (.topic record) conj (.value record))
             context)}))

(defn context []
  {:kafka-client
   (doto (new MockConsumer OffsetResetStrategy/EARLIEST)
     (.subscribe ["TOPIC_A"])
     (.rebalance [partition-0])
     (.updateBeginningOffsets {partition-0 0})
     (.seek partition-0 0)
     (.addRecord (new ConsumerRecord "TOPIC_A" 0 0 0 first-message))
     (.addRecord (new ConsumerRecord "TOPIC_A" 0 1 0 second-message)))

   :consume-interceptors [dummy-interceptor]

   :consumed-messages (atom {"TOPIC_A" []})})

(deftest consumer-loop
  (testing "polls records from broker and consumes them"
    (let [{:keys [consumed-messages]} (chain/execute (context) [nut/consumer-loop])]
      (Thread/sleep 10) ; give some time for the messages to be consumed
      (is (= ["hello" "bye"]
             (-> consumed-messages deref (get "TOPIC_A")))))))
