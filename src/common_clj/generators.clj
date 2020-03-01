(ns common-clj.generators
  (:require [clj-time.coerce :as c]
            [clojure.test.check.generators :as gen]
            [java-time :as j-time]
            [schema-generators.complete :as schema.complete]
            [schema-generators.generators :as schema.generators]))

#_(def ^:private day->ms (* 1000 60 60 24))
#_(defn- large-int->local-date [v]
    (-> v
        (* day->ms)
        c/from-long
        time/local-date-time->local-date))

#_(def ^:private local-date
    (gen/fmap large-int->local-date gen/large-integer))

(def ^:private local-date-time
  (gen/fmap (comp j-time/local-date-time c/from-long) gen/large-integer))

(def ^:private big-decimal
  (gen/fmap bigdec (gen/double* {:infinite? false :NaN? false})))

(def ^:private leaf-generators
  {;java.time.LocalDate     local-date
   java.time.LocalDateTime local-date-time
   BigDecimal              big-decimal})

(defn generate
  ([schema]
   (generate schema {}))
  ([schema custom-leaf-generators]
   (schema.generators/generate schema (merge leaf-generators
                                             custom-leaf-generators))))

(defn complete
  ([m schema]
   (complete m schema {}))
  ([m schema custom-leaf-generators]
   (schema.complete/complete m schema {} (merge  leaf-generators
                                                 custom-leaf-generators))))

(defn sample
  ([num-samples schema] (sample num-samples schema {}))
  ([num-samples schema custom-leaf-generators]
   (schema.generators/sample num-samples schema (merge leaf-generators
                                                       custom-leaf-generators))))
