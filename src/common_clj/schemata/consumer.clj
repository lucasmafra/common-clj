(ns common-clj.schemata.consumer
  (:require [schema.core :as s]))

(def ConsumerTopics
  {s/Keyword {:handler (s/pred clojure.test/function?)
              :schema {s/Keyword s/Any}}})
