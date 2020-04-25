(ns common-clj.kafka.consumer.config
  (:require [clojure.string :as str]
            [schema.core :as s])
  (:import org.apache.kafka.clients.consumer.ConsumerConfig
           org.apache.kafka.common.serialization.StringDeserializer))

(s/defn consumer-config [{:keys [kafka/brokers] :as config}]
  {ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG        (str/join "," brokers)
   ConsumerConfig/GROUP_ID_CONFIG                 (name (:app/name config))
   ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG   (.getName StringDeserializer)
   ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)})
