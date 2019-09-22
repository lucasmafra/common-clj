(ns common-clj.generators
  (:require [clojure.test.check.generators :as gen]
            [clj-time.coerce :as c]
            [clj-time.local :as l]
            [common-clj.time :as time]
            [java-time :as j-time]
            [schema-generators.generators :as schema.generators]
            [schema-generators.complete :as schema.complete]))

(def day->ms (* 1000 60 60 24))

(defn large-int->local-date [v]
  (-> v
      (* day->ms)
      c/from-long
      time/local-date-time->local-date))

(def local-date
  (gen/fmap large-int->local-date gen/large-integer))

(def local-date-time
  (gen/fmap (comp j-time/local-date-time c/from-long) gen/large-integer))

(def big-decimal (gen/fmap bigdec (gen/double* {:infinite? false :NaN? false})))

(defn generate
  [schema]
  (schema.generators/generate
   schema
   {java.time.LocalDate     local-date
    java.time.LocalDateTime local-date-time
    BigDecimal              big-decimal}))

(defn complete
  [m schema]
  (schema.complete/complete
   m
   schema
   {}
   {java.time.LocalDate     local-date
    java.time.LocalDateTime local-date-time
    BigDecimal              big-decimal}))
