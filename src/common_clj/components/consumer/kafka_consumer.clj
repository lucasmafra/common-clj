(ns common-clj.components.consumer.kafka-consumer
  (:require [cheshire.core :refer [parse-string]]
            [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.consumer.protocol :as consumer.protocol :refer [Consumer]]
            [common-clj.lib.kafka :refer [kafka-topic->topic topic->kafka-topic]]
            [common-clj.schemata.config :as schemata.config]
            [common-clj.schemata.consumer :as schemata.consumer]
            [schema.coerce :as coerce]
            [schema.core :as s])
  (:import java.util.Properties
           (org.apache.kafka.clients.consumer ConsumerConfig KafkaConsumer)
           (org.apache.kafka.common.errors InterruptException)
           (org.apache.kafka.common.serialization StringDeserializer)))

(defn coerce
  [schema message]
  (let [coercer (coerce/coercer schema coerce/json-coercion-matcher)]
    (coercer message)))

(defn ^:private consumer-props [config]
  (let [{:keys [app-name kafka-server]} (config.protocol/get-config config)
        props (Properties.)
        props-map [[ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG kafka-server]
                   [ConsumerConfig/GROUP_ID_CONFIG (str app-name)]
                   [ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)]
                   [ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)]
                   [ConsumerConfig/MAX_POLL_RECORDS_CONFIG (int 1)]
                   [ConsumerConfig/ENABLE_AUTO_COMMIT_CONFIG false]
                   [ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest"]]]
    (doseq [[k v] props-map] (.put props k v))
    props))

(defn ^:private consumer-loop
  [{:keys [kafka-client] :as component} consumer-topics]
  (Thread/sleep 100)
  (while true
    (let [records (.poll kafka-client 100)]
      (doseq [record records]
        (let [kafka-topic          (.topic record)
              topic                (kafka-topic->topic kafka-topic)
              raw-message          (.value record)
              message              (parse-string raw-message true)]
          (consumer.protocol/consume! component topic message))))))

(defn new-kafka-client [props]
  (KafkaConsumer. props))

(s/defrecord KafkaConsumerImpl [consumer-topics :- schemata.consumer/ConsumerTopics]
  component/Lifecycle
  (start [component]
    (let [kafka-topics (->> consumer-topics keys (map topic->kafka-topic))
          props        (consumer-props (:config component))
          kafka-client (new-kafka-client props)]
      (.subscribe kafka-client kafka-topics)
      (let [updated-component (assoc component :kafka-client kafka-client)
            loop              (Thread. #(consumer-loop updated-component consumer-topics))]
        (.start loop)
        (assoc updated-component :consumer-thread loop))))

  Consumer
  (consume! [component topic message]
    (let [handler         (get-in consumer-topics [topic :handler])
          schema          (get-in consumer-topics [topic :schema])
          coerced-message (coerce schema message)]
      (s/validate schema coerced-message)
      (handler coerced-message component))))

  (s/defn new-consumer [consumer-topics :- schemata.consumer/ConsumerTopics]
    (map->KafkaConsumerImpl {:consumer-topics consumer-topics}))
