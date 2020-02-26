(ns common-clj.schema-helpers
  (:require [clojure.walk :as walk]
            [schema.core :as s]))

(defn loose-schema [schema]
  (walk/postwalk
   (fn [form]
     (if (and (instance? clojure.lang.PersistentArrayMap form) (nil? (get form s/Keyword)))
       (assoc form s/Keyword s/Any)
       form))
   schema))
