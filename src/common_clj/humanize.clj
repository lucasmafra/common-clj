(ns common-clj.humanize

  (:require [clojure.core.match :refer [match]]
            [schema.core :as s]
            [schema.utils :refer [named-error-explain validation-error-explain]]
            [common-clj.misc :as misc])
  (:import [schema.utils NamedError ValidationError]))

(defn humanize
  [x]
  (let [String java.lang.String
        Number java.lang.Number
        LocalDate java.time.LocalDate]
    (match
     x

      ['not ['pos? num]]
      (str num " is not positive.")

      ['not ['instance? Number not-num]]
      (str "'" not-num "' is not a number.")

      ['not ['instance? String not-string]]
      (str "'" not-string "' is not a string.")

      ['not ['instance? LocalDate not-local-date]]
      (str "'" not-local-date "' is not a valid date")

      ['not [['between min max] given]]
      (str "The value must be between " min " and " max ". But given: " given)

      ['not [(enum :guard set?) given]]
      (str "The value must be one of the following: " enum ". But given: " given)

      ['not ['pos-int? num]]
      (str num " is not a positive integer.")

      ['not ['valid-email email]]
      (str email " is not a valid email.")

      ['named inner name]
      (humanize inner)

      :else
      (str x))))

(defn explain
  ([errors] (explain errors identity))
  ([errors translator]
   (cond
     (map? errors)
     (misc/map-vals #(explain % translator) errors)

     (or (seq? errors)
         (coll? errors))
     (mapv #(explain % translator) errors)

     (instance? NamedError errors)
     (translator (misc/vectorize (named-error-explain errors)))

     (instance? ValidationError errors)
     (translator (misc/vectorize (validation-error-explain errors)))

     :else
     errors)))

(defn check
  ([schema x] (check schema x identity))
  ([schema x translator]
   (some-> (s/check schema x) (explain translator))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Example usage:
#_(s/defschema PosNum
    (s/both s/Num (s/pred pos? 'pos?)))
#_(def my-map-schema {:x PosNum :y s/Num :z {:zz s/Str}})

#_(check {:a s/Str} {:a 1 :b 2} humanize);; => "4 is not a string but it should be"
#_(check PosNum -1 humanize) ;; => -1 is not positive but it should be
#_(check my-map-schema {:x 1 :y -2 :z {:zz 3}} humanize) ;;  => {:z {:zz "'3' is not a string but it should be."}}

;; Or without translator:
#_(check PosNum 0);; => [not [pos? 0]]
#_(check PosNum "fo");; => [not [instance? java.lang.Number 0]]

;; Or an example of a custom predicate which carries 
;; the data over to name of the predicate:
#_(defn between
    "showing that we can store arbitrary information about the schema in the name"
    [min max]
    (s/both s/Num
            (s/pred #(<= min % max)
                    `(~'between ~min ~max))))

#_(check (between 3 6) 7 humanize) ;; => "The value must be between 3 and 6. But given: 8"
#_(explain (s/check {:a s/Str :b s/Int} {:a 0 :b "a"}))
