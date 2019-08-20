(ns common-clj.components.consumer.kafka-consumer
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [common-clj.schemata.config :as schemata.config]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.schemata.consumer :as schemata.consumer]
            [common-clj.components.consumer.protocol :as consumer.protocol :refer [Consumer]])
  (:import java.util.Properties
           org.apache.kafka.clients.consumer.ConsumerConfig
           (org.apache.kafka.common.serialization StringDeserializer)
           (org.apache.kafka.common.errors InterruptException)))

(s/defn topic->kafka-topic [topic :- s/Keyword] :- s/Str
  (-> topic
      name
      clojure.string/upper-case
      (clojure.string/replace "-" "_")))

(s/defn kafka-topic->topic [kafka-topic :- s/Str] :- s/Keyword
  (-> kafka-topic
      clojure.string/lower-case
      (clojure.string/replace "_" "-")
      keyword))

(s/defrecord KafkaConsumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  component/Lifecycle
  (start [component]
    (let [kafka-topics (->> consumer-topics keys (map topic->kafka-topic))
          config-component (:config component)
          {:keys [app-name kafka-server]} (config.protocol/get-config config-component)
          props    (Properties.)
          config   [[ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG kafka-server]
                    [ConsumerConfig/GROUP_ID_CONFIG (str app-name)]
                    [ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)]
                    [ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)]
                    [ConsumerConfig/MAX_POLL_RECORDS_CONFIG (int 1)]
                    [ConsumerConfig/ENABLE_AUTO_COMMIT_CONFIG false]
                    [ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest"]]]
      (doseq [[k v] config] (.put props k v))
      (let [consumer (org.apache.kafka.clients.consumer.KafkaConsumer. props)]
        (.subscribe consumer kafka-topics)
        (let [updated-component (assoc component :consumer consumer)
              consumer-thread (Thread. #(consumer.protocol/run updated-component))]
          (.start consumer-thread)
          (assoc updated-component :consumer-thread consumer-thread)))))
  
  (stop [{:keys [consumer consumer-thread] :as component}]
    (.stop consumer-thread)
    (-> component
        (assoc :consumer nil)
        (assoc :consumer-thread nil)))
  
  Consumer
  (run [{:keys [consumer] :as component}]
    (try
      (while true
        (let [records (.poll consumer 100)]
          (doseq [record records]
            (let [topic (kafka-topic->topic (.topic record))
                  handler (get-in consumer-topics [topic :handler])
                  schema (get-in consumer-topics [topic :schema])
                  record (.value record)]
              (handler record)))))
      (finally
        (try
          (.close consumer)
          (catch InterruptException e))))))

(s/defn new-consumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  (map->KafkaConsumer {:consumer-topics consumer-topics}))
