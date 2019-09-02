(ns common-clj.coercion
  (:require [java-time :refer [local-date local-date-time]]
            [schema-tools.coerce :as stc]
            [schema.coerce :as coerce]))

(defn big-decimal-matcher [schema]
  (when (= java.math.BigDecimal schema)
    (coerce/safe bigdec)))

(defn local-date-matcher [schema]
  (when (= java.time.LocalDate schema)
    (coerce/safe local-date)))

(defn local-date-time-matcher [schema]
  (when (= java.time.LocalDateTime schema)
    (coerce/safe local-date-time)))

(def json-matcher
  "Extends built-in schema json matcher to support more
   types like BigDecimals and LocalDate/LocalDateTime"
  (coerce/first-matcher [local-date-matcher
                         local-date-time-matcher
                         big-decimal-matcher
                         coerce/json-coercion-matcher]))

(defn coerce [schema data]
  (stc/coerce data schema json-matcher))

