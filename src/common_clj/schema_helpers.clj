(ns common-clj.schema-helpers
  (:require [schema.core :as s]))

(defn loose-schema [schema]
  (assoc schema s/Keyword s/Any))

