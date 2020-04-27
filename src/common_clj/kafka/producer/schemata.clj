(ns common-clj.kafka.producer.schemata
  (:require [common-clj.schema.helpers :as csh]
            [schema.core :as s]))

(def Topic s/Keyword)

(def TopicSettings
  {:schema s/Any
   :topic  s/Str})

(def Topics
  {Topic TopicSettings})

(def KafkaConfig
  (csh/loose-schema
   #:kafka {:brokers [s/Str]}))
