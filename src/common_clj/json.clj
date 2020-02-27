(ns common-clj.json
  (:require [cheshire.core :refer [generate-string parse-string]]))

(defn zone [] (java.time.ZoneId/systemDefault))

(def ^:private special-type->supported-type
  {java.time.LocalDate str
   java.time.LocalDateTime str})

(defn to-supported-type [value]
  (let [conversion-fn (or (special-type->supported-type (class value))
                          identity)]
    (conversion-fn value)))

(defn json->string [json]
  (->> json
       (clojure.walk/postwalk to-supported-type)
       generate-string))

(defn string->json
  ([string]
   (string->json string true))
  ([string keywordize]
   (parse-string string keywordize)))
