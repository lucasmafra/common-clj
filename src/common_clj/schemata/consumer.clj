(ns common-clj.schemata.consumer
  (:require [clojure.test :refer [function?]]
            [schema.core :as s]))

(def ConsumerTopics
  {s/Keyword {:handler (s/pred function?)
              :schema  {s/Keyword s/Any}}})
