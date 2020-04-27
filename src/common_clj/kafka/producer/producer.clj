(ns common-clj.kafka.producer.producer
  (:require [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as conf-pro]
            [common-clj.kafka.producer.config :as producer-config]
            [common-clj.kafka.producer.interceptors.json-serializer :as i-json-serializer]
            [common-clj.kafka.producer.interceptors.mock-produced-record :as i-mock-record]
            [common-clj.kafka.producer.interceptors.sender :as i-sender]
            [common-clj.kafka.producer.protocol :as producer-pro]
            [common-clj.kafka.producer.schemata :as s-producer]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import java.util.Properties
           org.apache.kafka.clients.producer.MockProducer))

(def default-interceptors
  [i-json-serializer/json-serializer
   i-sender/sender])

(defn build-interceptors [env]
  (cond-> default-interceptors
    (= env :test) (conj i-mock-record/mock-produced-record)))

(defn- ->props [config-map]
  (doto (Properties.)
    (.putAll (producer-config/producer-config config-map))))

(defn init-kafka-client [props]
  (new org.apache.kafka.clients.producer.KafkaProducer props))

(defn mock-kafka-client [] (new MockProducer))

(s/defrecord KafkaProducer [topics :- s-producer/Topics]
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [config-map (conf-pro/get-config config)
          env        (conf-pro/get-env config)]
      (cond-> component
        true          (assoc :kafka-client (init-kafka-client (->props config-map)))
        (= env :test) (merge {:kafka-client   (mock-kafka-client)
                              :produced-records (atom {:records []})}))))

  (stop [{:keys [kafka-client] :as component}]
    (.close kafka-client)
    component)

  producer-pro/Producer
  (produce! [{:keys [kafka-client config produced-records]} topic message]
    (let [env             (conf-pro/get-env config)
          interceptors    (build-interceptors env)
          initial-context {:topic            topic
                           :topics           topics
                           :message          message
                           :kafka-client     kafka-client
                           :produced-records produced-records}]
      (chain/execute initial-context interceptors))))

(s/defn new-producer [topics :- s-producer/Topics]
  (map->KafkaProducer {:topics topics}))
