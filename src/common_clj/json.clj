(ns common-clj.json
  (:require [cheshire.core :refer [generate-string parse-string]]
            [common-clj.misc :as misc]
            [common-clj.schema :as cs]
            [schema.core :as s]
            [schema.spec.core :as spec]))

(def default-serialization-map
  {cs/LocalDate             str
   cs/LocalDateTime         str
   cs/LocalTime             str
   cs/UTCDateTime           str
   cs/EpochMillis           #(.toEpochMilli %)})

(defn transform-values [schema serialization-map]
  (spec/run-checker,
   (fn [s params]
     (let [walk (spec/checker (s/spec s) params)]
       (fn [x]
         (let [result (walk x)
               conversion-fn (or (serialization-map s)
                                 identity)]
           (conversion-fn result)))))
   true
   schema))

(def default-values
  {:transform-fns [misc/underscore->dash]
   :serialization-map default-serialization-map})

(defn json->string
  ([json]
   (json->string json s/Any))
  ([json schema]
   (json->string json schema default-values))
  ([json schema {:keys [serialization-map transform-fns]}]
   (s/validate schema json)
   (->> json
        ((transform-values schema serialization-map))
        ((apply comp transform-fns))
        generate-string)))

(defn string->json
  ([string]
   (string->json string true))
  ([string key-fn]
   (parse-string string key-fn)))
