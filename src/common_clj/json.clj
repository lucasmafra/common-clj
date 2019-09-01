(ns common-clj.json
  (:require [cheshire.core :refer [generate-string]]))

(defn zone [] (java.time.ZoneId/systemDefault))

(def ^:private special-type->supported-type
  {java.time.LocalDate str
   java.time.LocalDateTime #(-> %
                                (.atZone (zone))
                                .toInstant
                                java.util.Date/from)})

(defn to-supported-type [value]
  (let [conversion-fn (or (special-type->supported-type (class value))
                          identity)]
    (conversion-fn value)))

(defn json->string [json]
  (->> json
       (map (fn [[k v]] {k (to-supported-type v)}))
       (into {})
       generate-string))



