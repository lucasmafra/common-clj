(ns common-clj.components.consumer.in-memory-consumer-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.consumer.in-memory-consumer :as in-memory-consumer]
            [common-clj.components.consumer.protocol :as consumer.protocol]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.test-helpers :refer [init! message-arrived! schema-error? try-consume!]]
            [matcher-combinators.midje :refer [match]]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [selvage.midje.flow :refer [*world* flow]]))

(s/defschema SchemaA
  {:field1 s/Str
   :field2 s/Int
   :field3 s/Keyword})

(s/def valid-message :- SchemaA
  {:field1 "abc"
   :field2 123
   :field3 :abc})

(def invalid-message
  {:field1 123
   :field2 "abc"})

(s/defn handler-a [message :- SchemaA {:keys [counter-a logger]}]
  "Increments counter :a every time it is called"
  (counter.protocol/inc! counter-a)
  (logger.protocol/log! logger :message message))

(s/defn handler-b [message :- s/Any {:keys [counter-b]}]
  "Increments counter :b every time it is called"
  (counter.protocol/inc! counter-b))

(def consumer-topics
  {:topic-a   
   {:handler handler-a
    :schema  SchemaA}
   
   :topic-b
   {:handler handler-b
    :schema  s/Any}})

(def system
  (component/system-map
   :counter-a (in-memory-counter/new-counter)
   :counter-b (in-memory-counter/new-counter)
   :logger    (in-memory-logger/new-logger)
   :consumer  (component/using
               (in-memory-consumer/new-consumer consumer-topics)
               [:counter-a :counter-b :logger])))

(flow "valid message arrives"
 (partial init! system)

 (partial message-arrived! :topic-a valid-message)

 (fact "handler of corresponding topic was called"
   (-> *world* :system :counter-a counter.protocol/get-count)
   => 1)

 (fact "handler of different topic wasn't called"
   (-> *world* :system :counter-b counter.protocol/get-count)
   => 0)

 (fact "the incoming message is passed to handler"
   (-> *world* :system :logger (logger.protocol/get-logs :message))
   => [valid-message]))

(flow "invalid message arrives to topic"
  (partial init! system)

  (partial try-consume! :topic-a invalid-message)

  (fact "schema error was thrown"
    (-> *world* :consumption-errors :topic-a)
    => (match [schema-error?]))

  (fact "handler didn't consume the message"
    (-> *world* :system :counter-a counter.protocol/get-count)
    => 0))