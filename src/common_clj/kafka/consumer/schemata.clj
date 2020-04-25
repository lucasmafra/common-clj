(ns common-clj.kafka.consumer.schemata
  (:require [schema.core :as s]
            [common-clj.schema.helpers :as csh]))

(def KafkaConfig
  (csh/loose-schema
   {:kafka/brokers [s/Str]}))
