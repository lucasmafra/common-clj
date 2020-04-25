(ns common-clj.kafka.consumer.interceptors.kafka-client
  (:require [clojure.string :as str]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s])
  (:import java.util.Properties
           [org.apache.kafka.clients.consumer ConsumerConfig KafkaConsumer]
           org.apache.kafka.common.serialization.StringDeserializer))

(s/defn consumer-config [{:keys [kafka/brokers] :as config}]
  {ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG        (str/join "," brokers)
   ConsumerConfig/GROUP_ID_CONFIG                 (name (:app/name config))
   ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG   (.getName StringDeserializer)
   ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG (.getName StringDeserializer)})

(defn- ->props [config]
  (doto (Properties.) (.putAll (consumer-config config))))

(def kafka-client
  (interceptor/interceptor
   {:name  ::kafka-client
    :enter (fn [{:keys [config] :as context}]
             (assoc context :kafka-client (new KafkaConsumer (->props config))))}))
