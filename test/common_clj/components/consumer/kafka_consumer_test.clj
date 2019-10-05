(ns common-clj.components.consumer.kafka-consumer-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [common-clj.components.consumer.kafka-consumer :as kafka-consumer]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.test-helpers :refer [coercion-error? init! kafka-message-arrived!
                                             kafka-try-consume! mock-kafka-client]]
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
  "Increments counter :a and logs the received message"
  (counter.protocol/inc! counter-a)
  (logger.protocol/log! logger :message message))

(s/defn handler-b [message :- s/Any {:keys [counter-b]}]
  "Increments counter :b"
  (counter.protocol/inc! counter-b))

(def consumer-topics
  {:topic-a   
   {:handler handler-a
    :schema  SchemaA}
   
   :topic/b
   {:handler handler-b
    :schema  s/Any}})

(def app-config
  {:app-name :common-clj
   :kafka-server "localhost:9092"})

(def system
  (component/system-map
   :config    (in-memory-config/new-config app-config)
   :counter-a (in-memory-counter/new-counter)
   :counter-b (in-memory-counter/new-counter)
   :logger    (in-memory-logger/new-logger)
   :consumer  (component/using
               (kafka-consumer/new-consumer consumer-topics)
               [:config :counter-a :counter-b :logger])))

(with-redefs [kafka-consumer/new-kafka-client mock-kafka-client]
  (flow "consumer started"
    (future-fact "consumer group matches :app-name passed in config")
    (future-fact "it listens to kafka server passed to config via :kafka-server"))

  (flow "consumer started but the kafka server is down"
    (future-fact "it throws error on component/start"))

  (flow "valid message arrives"
    (partial init! system)

    (partial kafka-message-arrived! "TOPIC_A" valid-message)
        
    (fact "handler of corresponding topic was called"
      (-> *world* :system :counter-a counter.protocol/get-count)
      => 1)
    
    (fact "handler of different topic wasn't called"
      (-> *world* :system :counter-b counter.protocol/get-count)
      => 0)
    
    (fact "the incoming message is passed to handler"
      (-> *world* :system :logger (logger.protocol/get-logs :message))
      => [valid-message]))

  (flow "invalid message arrives"
    (partial init! system)

    (partial kafka-try-consume! "TOPIC_A" invalid-message)
    
    (fact "schema error was thrown"
      (-> *world* :system :logger (logger.protocol/get-logs "TOPIC_A"))
      => (match [coercion-error?]))
    
    (fact "handler didn't consume the message"
      (-> *world* :system :counter-a counter.protocol/get-count)
      => 0)))
