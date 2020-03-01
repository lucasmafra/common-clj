(ns common-clj.components.producer.in-memory-producer-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.producer.in-memory-producer
             :as
             in-memory-producer]
            [schema.core :as s]))

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

(def system
  (component/system-map
   :producer (in-memory-producer/new-producer producer-topics)))

#_(flow "produce valid message"
        (partial init! system)

        (partial produce! :topic-a valid-message)

        (fact "message is produced to correct topic"
              (produced-messages :topic-a) => (match [valid-message])
              (produced-messages :topic-b) => []))

#_(flow "try to produce invalid message"
        (partial init! system)

        (partial try-produce! :topic-a invalid-message)

        (fact "schema error is thrown"
              (produced-errors :topic-a) => (match [schema-error?]))

        (fact "no message is actually produced"
              (produced-messages :topic-a) => []))

#_(flow "try to produce to an unknown topic"
        (partial init! system)

        (partial try-produce! :unknown-topic valid-message)

        (fact "error is thrown"
              (produced-errors :unknown-topic) => (match [exception?])))
