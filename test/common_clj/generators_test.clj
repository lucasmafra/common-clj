(ns common-clj.generators-test
  (:require  [midje.sweet :refer :all]
             [clojure.test.check.generators :as gen]
             [common-clj.generators :as g]
             [schema.core :as s]))

(defn- valid? [schema example] (= (s/validate schema example) example))
(def my-uuid #uuid "2c44e59a-e612-4d2e-bf0f-5bbd6afeda8f")
(def SchemaA
  {:a                  s/Uuid
   :b                  s/Keyword
   :c                  BigDecimal
   :d                  java.time.LocalDate
   (s/optional-key :e) java.time.LocalDateTime
   :f                  s/Str})

(fact "large-int->local-date"
  (g/large-int->local-date 0) => #date "1970-01-01"
  (g/large-int->local-date 1) => #date "1970-01-02"
  (count (set (map g/large-int->local-date (range -10 10)))) => 20)

(facts "g/local-date"  
  (fact "result is LocalDate"
    (class (gen/generate g/local-date)) => java.time.LocalDate))

(facts "g/local-date-time"
  (fact "result is LocalDateTime"
    (class (gen/generate g/local-date-time)) => java.time.LocalDateTime))

(facts "g/big-decimal"
  (fact "result is BigDecimal"
    (class (gen/generate g/big-decimal)) => java.math.BigDecimal))

(facts "generate"
  (fact "valid example"
    (valid? SchemaA (g/generate SchemaA)) => true))

(facts "complete"
  (fact "generates example using given values"
    (:a (g/complete {:a my-uuid} SchemaA)) => my-uuid)
  (fact "result conforms to schema"
    (valid? SchemaA (g/complete {} SchemaA)) => true))
