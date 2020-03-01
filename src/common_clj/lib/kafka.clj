(ns common-clj.lib.kafka
  (:require clojure.string
            [schema.core :as s]))

(s/defn topic->kafka-topic [topic :- s/Keyword] :- s/Str
  (-> topic
      name
      clojure.string/upper-case
      (clojure.string/replace "-" "_")))

(s/defn kafka-topic->topic [kafka-topic :- s/Str] :- s/Keyword
  (-> kafka-topic
      clojure.string/lower-case
      (clojure.string/replace "_" "-")
      keyword))
