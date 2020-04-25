(ns common-clj.kafka.consumer.schemata
  (:require [common-clj.schema.helpers :as csh]
            [schema.core :as s]))

(def KafkaConfig
  (csh/loose-schema
   {:kafka/brokers [s/Str]}))
