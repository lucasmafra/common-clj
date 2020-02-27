(ns common-clj.json
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clojure.walk :as walk]))

(defn- transform-values [json serialization-map]
  (walk/postwalk
   (fn [form]
     (let [conversion-fn (or (serialization-map (class form))
                             identity)]
       (conversion-fn form)))
   json))

(def default-serialization-map
  {java.time.LocalDate str
   java.time.LocalDateTime str})

(defn json->string
  ([json]
   (json->string json default-serialization-map))
  ([json serialization-map]
   (-> json
       (transform-values serialization-map)
       generate-string)))

(defn string->json
  ([string]
   (string->json string true))
  ([string key-fn]
   (parse-string string key-fn)))
