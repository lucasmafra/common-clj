(ns common-clj.kafka.producer.producer
  (:require [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as conf-pro]
            [common-clj.kafka.producer.config :as producer-config]
            [common-clj.kafka.producer.interceptors.json-serializer :as i-json-serializer]
            [common-clj.kafka.producer.interceptors.sender :as i-sender]
            [common-clj.kafka.producer.interceptors.topic-name :as i-topic-name]
            [common-clj.kafka.producer.protocol :as producer-pro]
            [common-clj.kafka.producer.schemata :as s-producer]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import java.util.Properties))

(def default-interceptors
  [i-topic-name/topic-name
   i-json-serializer/json-serializer
   i-sender/sender])

(def build-interceptors (constantly default-interceptors))

(defn- ->props [config-map]
  (doto (Properties.)
    (.putAll (producer-config/producer-config config-map))))

(defn init-kafka-producer [props]
  (new org.apache.kafka.clients.producer.KafkaProducer props))

(s/defrecord KafkaProducer [topics :- s-producer/Topics]
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [config-map (conf-pro/get-config config)]
      (-> component
          (assoc :kafka-producer (init-kafka-producer (->props config-map)))
          (assoc :produced-records (atom {:records []})))))

  (stop [{:keys [kafka-producer] :as component}]
    (.close kafka-producer)
    (dissoc component :kafka-producer))

  producer-pro/Producer
  (produce! [{:keys [kafka-producer config]} topic message]
    (let [env             (conf-pro/get-env config)
          interceptors    (build-interceptors env)
          initial-context {:topic    topic
                           :topics   topics
                           :message  message
                           :producer kafka-producer}]
      (chain/execute initial-context interceptors))))

(s/defn new-producer [topics :- s-producer/Topics]
  (map->KafkaProducer {:topics topics}))
