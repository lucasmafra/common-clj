(ns common-clj.lib.kafka-test
  (:require [common-clj.lib.kafka :as kafka]
            [common-clj.schemata.consumer :as s-consumer]
            [midje.experimental :refer [for-all]]
            [midje.sweet :refer :all]))

#_(facts "topic->kafka-topic"
  (fact "capitalize and underscore"
    (kafka/topic->kafka-topic :create-transaction) => "CREATE_TRANSACTION"))

#_(for-all
  [topic s-consumer/topic-name-generator]
  (fact "kafka-topic->topic is the inverse fn of topic->kafka-topic"
    (-> topic kafka/topic->kafka-topic kafka/kafka-topic->topic) => topic))
