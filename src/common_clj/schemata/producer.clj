(ns common-clj.schemata.producer
  (:require [schema.core :as s]))

(def ProducerTopics
  {s/Keyword {s/Keyword s/Any}})
