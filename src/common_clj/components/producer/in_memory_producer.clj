(ns common-clj.components.producer.in-memory-producer
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.producer.protocol :refer [Producer]]
            [common-clj.schemata.producer :as schemata.producer]
            [schema.core :as s]))

(s/defrecord InMemoryProducer [producer-topics]
  component/Lifecycle
  (start [component]
    (assoc component :messages (atom {})))

  (stop [component])

  Producer
  (produce! [component topic message]
    (let [schema (topic producer-topics)]
      (s/validate schema message)
      (-> component
          :messages
          (swap! (fn [messages] (update messages topic #(conj % message))))))))

(s/defn new-producer [producer-topics :- schemata.producer/ProducerTopics]
  (map->InMemoryProducer {:producer-topics producer-topics}))
