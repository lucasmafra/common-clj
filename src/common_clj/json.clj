(ns common-clj.json
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clojure.walk :as walk]
            [common-clj.schema :as cs]
            [schema.core :as s]
            [common-clj.misc :as misc]))

(def default-serialization-map
  {cs/LocalDate             str
   cs/LocalDateTime         str
   cs/LocalTime             str
   cs/UTCDateTime           str
   cs/EpochMillis           #(.toEpochMilli %)})

(defn transform-values [json ks schema serialization-map]
  (if (map? json)
    (misc/map-vals-with-key #(transform-values %2 (conj ks %1) schema serialization-map) json)
    (let [v-schema (get-in schema ks)
          conversion-fn (or (serialization-map v-schema)
                            identity)]
      (conversion-fn json))))

(defn json->string
  ([json]
   (json->string json {}))
  ([json schema]
   (json->string json schema default-serialization-map))
  ([json schema serialization-map]
   (-> json
       (transform-values [] schema serialization-map)
       generate-string)))

(defn string->json
  ([string]
   (string->json string true))
  ([string key-fn]
   (parse-string string key-fn)))
