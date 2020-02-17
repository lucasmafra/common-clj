(ns common-clj.humanize

  (:require [clojure.core.match :refer [match]]
            [schema.core :as s]
            [common-clj.lib.utils :refer [map-vals vectorize]]
            [schema.utils :refer [named-error-explain validation-error-explain]])
  (:import [schema.utils NamedError ValidationError]))

(defn humanize
  "Returns a human explanation of a SINGLE error.
   This is for errors which are from USER input. It is not for programming errors.
   If the error is a map/vector then this function must be applied to each of those
   values.
   You should adapat this function for your own custom errors.
   This is just an example. Don't actually use this function.
   Define it in your business logic and pass it in to the check function."
  [x]
  ;; http://stackoverflow.com/questions/25189031/clojure-core-match-cant-match-on-class
  (let [;; TLDR: We can't match on classes (it'd be bound to that symbol)
        ;; However, match will first try to match a local binding if it exists:
        String java.lang.String
        Number java.lang.Number
        LocalDate java.time.LocalDate]
    (match
      x
      ;;;;;;;;;;;;;;;;;;;;; DEAL with most common stuff
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

      ;; We can use core.match's :guard to apply a function check:
      ;; error by s/enum: (not (#{:x :y} :foo))
      ['not [(enum :guard set?) given]]
      (str "The value must be one of the following: " enum ". But given: " given)

      ['not ['pos-int? num]]
      (str num " is not a positive integer.")
      ;; TODO: Add much more cases which depend on your business logic.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;; Pluck out the error in case of a named error
      ['named inner name]
      (humanize inner)

      ;; likely a programming error. Needs fixed from either client or server code:
      :else
      (str x)
      )))

(defn explain
  "Takes an error object as returned from schema an transforms each leaf value of either
   1. NamedError
   2. ValidationError
   Such that it is 'explained' (like schema's explain) but additionally
   turns the results into vectors.
   Optionally takes a translator if you want the NamedErrors & ValidationErrors explained
   in a humanized form.
   "
  ([errors] (explain errors identity))
  ([errors translator]
   (cond
     (map? errors)
     (map-vals #(explain % translator) errors)

     (or (seq? errors)
         (coll? errors))
     (mapv #(explain % translator) errors)

     (instance? NamedError errors)
     (translator (vectorize (named-error-explain errors)))

     (instance? ValidationError errors)
     (translator (vectorize (validation-error-explain errors)))

     :else
     errors)))

(defn check
  "Check x against schema and explain the errors.
   See explain for details of output format.<
   Just like schema's check, returns nil if no error.
  (check s/Str 4) ;; => [not [instance? java.lang.String 4]]"
  ([schema x] (check schema x identity))
  ([schema x translator]
   (some-> (s/check schema x) (explain translator))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Example usage:
(s/defschema PosNum
  (s/both s/Num (s/pred pos? 'pos?)))
(def my-map-schema {:x PosNum :y s/Num :z {:zz s/Str}})

(check {:a s/Str} {:a 1 :b 2} humanize);; => "4 is not a string but it should be"
(check PosNum -1 humanize) ;; => -1 is not positive but it should be
(check my-map-schema {:x 1 :y -2 :z {:zz 3}} humanize) ;;  => {:z {:zz "'3' is not a string but it should be."}}

;; Or without translator:
(check PosNum 0);; => [not [pos? 0]]
(check PosNum "fo");; => [not [instance? java.lang.Number 0]]

;; Or an example of a custom predicate which carries 
;; the data over to name of the predicate:
(defn between
  "showing that we can store arbitrary information about the schema in the name"
  [min max]
  (s/both s/Num
          (s/pred #(<= min % max)
                  `(~'between ~min ~max))))

(check (between 3 6) 7 humanize) ;; => "The value must be between 3 and 6. But given: 8"
(explain (s/check {:a s/Str :b s/Int} {:a 0 :b "a"}))
