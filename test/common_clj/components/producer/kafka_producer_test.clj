(ns common-clj.components.producer.kafka-producer-test
  (:require [cheshire.core :refer [generate-string]]
            [com.stuartsierra.component :as component]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [common-clj.components.producer.kafka-producer :as kafka-producer]
            [common-clj.test-helpers :refer [check-kafka-produced-errors check-kafka-produced-messages exception?
                                             init! kafka-produce!
                                             kafka-try-produce! mock-kafka-producer
                                             schema-error?]]
            [matcher-combinators.midje :refer [match]]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [selvage.midje.flow :refer [flow]]))

(s/defschema SchemaA
  {:field1 s/Str
   :field2 s/Int
   :field3 s/Keyword})

(def producer-topics
  {:topic-a SchemaA
   :topic-b s/Any})

(s/def valid-message :- SchemaA
  {:field1 "abc"
   :field2 123
   :field3 :abc})

(def invalid-message
  {:field1 123
   :field2 "abc"})

(def app-config
  {:app-name :common-clj
   :kafka-server "localhost:9092"})

(def system
  (component/system-map
   :config   (in-memory-config/new-config app-config)
   :producer (component/using
              (kafka-producer/new-producer producer-topics)
              [:config])))

#_(with-redefs [kafka-producer/new-kafka-client mock-kafka-producer]
  (flow "produce valid message"
    (partial init! system)

    (partial kafka-produce! :topic-a valid-message)
        
    (fact "message is produced to correct topic"
      (check-kafka-produced-messages "TOPIC_A") => [(generate-string valid-message)]
      (check-kafka-produced-messages "TOPIC_B") => []))

  (flow "try to produce invalid message"
    (partial init! system)

    (partial kafka-try-produce! :topic-a invalid-message)

    (fact "schema error is thrown"
      (check-kafka-produced-errors :topic-a) => (match [schema-error?]))
    
    (fact "no message is actually produced"
      (check-kafka-produced-messages :topic-a) => []))

  (flow "try to produce to an unknown topic"
    (partial init! system)

    (partial kafka-try-produce! :unknown-topic valid-message)

    (fact "error is thrown"
      (check-kafka-produced-errors :unknown-topic) => (match [exception?]))))
