(ns common-clj.components.producer.kafka-producer
  (:require [common-clj.components.producer.protocol :refer [Producer]]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [common-clj.schemata.producer :as schemata.producer]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.lib.kafka :refer [topic->kafka-topic]]
            [cheshire.core :refer [generate-string]])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerConfig ProducerRecord)
           (java.util Properties)
           (org.apache.kafka.common.serialization StringSerializer)))

(defn producer-props [config]
  (let [props (Properties.)
        {:keys [kafka-server]} (config.protocol/get-config config)
        props-map [[ProducerConfig/BOOTSTRAP_SERVERS_CONFIG kafka-server]
                   [ProducerConfig/ACKS_CONFIG "all"]
                   [ProducerConfig/RETRIES_CONFIG (int 5)]
                   [ProducerConfig/BATCH_SIZE_CONFIG (int 16384)]
                   [ProducerConfig/LINGER_MS_CONFIG (int 1)]
                   [ProducerConfig/BUFFER_MEMORY_CONFIG (int 33554432)]
                   [ProducerConfig/KEY_SERIALIZER_CLASS_CONFIG (.getName StringSerializer)]
                   [ProducerConfig/VALUE_SERIALIZER_CLASS_CONFIG (.getName StringSerializer)]]]
    (doseq [[k v] props-map] (.put props k v))
    props))

(defn new-kafka-client [props]
  (KafkaProducer. props))

(s/defn build-record [kafka-topic :- s/Str message]
  (ProducerRecord. kafka-topic (generate-string message)))

(s/defrecord KafkaProducerImpl [producer-topics]
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [props        (producer-props config)
          kafka-client (new-kafka-client props)]
      (assoc component :kafka-client kafka-client)))

  (stop [component]
    component)

  Producer
  (produce! [component topic message]
    (let [{:keys [kafka-client]} component
          schema (topic producer-topics)
          kafka-topic (topic->kafka-topic topic)
          record (build-record kafka-topic message)]
      (s/validate schema message)
      (.send kafka-client record))))

(s/defn new-producer [producer-topics :- schemata.producer/ProducerTopics]
  (map->KafkaProducerImpl {:producer-topics producer-topics}))
