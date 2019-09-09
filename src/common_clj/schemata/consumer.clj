(ns common-clj.schemata.consumer
  (:require [schema.core :as s]
            [clojure.test :refer [function?]]))

(def ConsumerTopics
  {s/Keyword {:handler (s/pred function?)
              :schema  {s/Keyword s/Any}}})
