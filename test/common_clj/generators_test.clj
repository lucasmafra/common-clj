(ns common-clj.generators-test
  (:require  [clojure.string :as str]
             [clojure.test.check.generators :as tc-gen]
             [common-clj.generators :as gen]
             [midje.sweet :refer :all]
             [schema.core :as s]))

(defn- valid? [schema example] (= (s/validate schema example) example))
(def my-uuid #uuid "2c44e59a-e612-4d2e-bf0f-5bbd6afeda8f")
(defn- lower-case? [v] (= (str/lower-case v) (str v)))
(defn- no-underscore? [v] (nil? (re-find #"_" (str v))))

(def SchemaA
  {:uuid            s/Uuid
   :keyword         s/Keyword
   :bigdecimal      BigDecimal
   :local-date      java.time.LocalDate
   :local-date-time java.time.LocalDateTime
   :string          s/Str})

(def valid-custom-type? (every-pred keyword? lower-case? no-underscore?))

(def CustomType
  (s/pred valid-custom-type?))

(def CustomSchema
  (assoc SchemaA :custom-type CustomType))

(def CustomGenerator
  (tc-gen/such-that valid-custom-type? tc-gen/keyword))

(facts "generate"
  (fact "valid example"
    (valid? SchemaA (gen/generate SchemaA)) => true)
  (fact "accepts custom leaf-generators"
    (valid? CustomSchema
            (gen/generate CustomSchema {CustomType CustomGenerator}))
    => true))

(facts "complete"
  (fact "generates example using given values"
    (:uuid (gen/complete {:uuid my-uuid} SchemaA)) => my-uuid)
  (fact "result conforms to schema"
    (valid? SchemaA (gen/complete {} SchemaA)) => true)
  (fact "accepts custom leaf-generators"
    (valid? CustomSchema
            (gen/complete {:uuid my-uuid} CustomSchema {CustomType CustomGenerator}))
    => true))

(facts "sample"
  (fact "generates the given number of examples for the given schema"
    (let [examples (gen/sample 15 SchemaA)]
      (count examples) => 15
      (doseq [example examples]
        (valid? SchemaA example) => true)))
  (fact "accepts custom leaf-generators"
    (let [examples (gen/sample 10 CustomSchema {CustomType CustomGenerator})]
      (count examples) => 10
      (doseq [example examples]
        (valid? CustomSchema example) => true))))
