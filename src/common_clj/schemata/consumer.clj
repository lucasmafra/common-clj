(ns common-clj.schemata.consumer
  (:require [clojure.string :as str]
            [clojure.test :refer [function?]]
            [clojure.test.check.generators :as tc-gen]
            [schema.core :as s]))

(defn- lower-case? [v]
  (= (str/lower-case v) (str v)))

(defn- no-underscore? [v]
  (nil? (re-find #"_" (str v))))

(def ^:private valid-topic-name? (every-pred keyword?
                                             lower-case?
                                             no-underscore?))
(def TopicName
  (s/pred valid-topic-name?))

(def topic-name-generator
  (tc-gen/such-that valid-topic-name? tc-gen/keyword))

(def ConsumerTopics
  {TopicName {:topic/handler (s/pred function?)
              :topic/schema  {s/Keyword s/Any}}})
