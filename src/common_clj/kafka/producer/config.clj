(ns common-clj.kafka.producer.config
  (:import org.apache.kafka.clients.producer.ProducerConfig
           org.apache.kafka.common.serialization.StringSerializer)
  (:require [clojure.string :as str]
            [common-clj.kafka.producer.schemata :as s-producer]
            [schema.core :as s]))

(s/defn producer-config [{:keys [kafka/brokers]} :- s-producer/KafkaConfig]
  {ProducerConfig/BOOTSTRAP_SERVERS_CONFIG      (str/join "," brokers)
   ProducerConfig/KEY_SERIALIZER_CLASS_CONFIG   (.getName StringSerializer)
   ProducerConfig/VALUE_SERIALIZER_CLASS_CONFIG (.getName StringSerializer)})
