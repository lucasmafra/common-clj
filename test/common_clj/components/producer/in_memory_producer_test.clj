(ns common-clj.components.producer.in-memory-producer-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [selvage.midje.flow :refer [*world* flow]]
            [matcher-combinators.midje :refer [match]]
            [com.stuartsierra.component :as component]
            [common-clj.test-helpers :refer [init! produce! schema-error? try-produce!
                                             check-produced-messages check-produced-errors
                                             exception?]]
            [common-clj.components.producer.in-memory-producer :as in-memory-producer]))

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

(flow "produce valid message"
  (partial init! system)

  (partial produce! :topic-a valid-message)

  (fact "message is produced to correct topic"
    (check-produced-messages :topic-a) => (match [valid-message])
    (check-produced-messages :topic-b) => []))

(flow "try to produce invalid message"
  (partial init! system)

  (partial try-produce! :topic-a invalid-message)

  (fact "schema error is thrown"
    (check-produced-errors :topic-a) => (match [schema-error?]))

  (fact "no message is actually produced"
    (check-produced-messages :topic-a) => []))

(flow "try to produce to an unknown topic"
  (partial init! system)

  (partial try-produce! :unknown-topic valid-message)

  (fact "error is thrown"
    (check-produced-errors :unknown-topic) => (match [exception?])))
