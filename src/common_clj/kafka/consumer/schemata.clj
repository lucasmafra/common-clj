(ns common-clj.kafka.consumer.schemata
  (:require [common-clj.schema.helpers :as csh]
            [schema.core :as s]))

(def KafkaConfig
  (csh/loose-schema
   {:kafka/brokers [s/Str]}))

(def Topic s/Keyword)

(def TopicSettings
  {:schema  s/Any
   :topic   s/Str
   :handler s/Any})

(def Topics
  {Topic TopicSettings})
