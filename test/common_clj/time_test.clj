(ns common-clj.time-test
  (:require [midje.sweet :refer :all]
            [common-clj.time :as time]
            [schema.core :as s])
  (:import java.time.LocalDate))

(defn from-string [date] (LocalDate/parse date))

(def monday #date "2019-09-16")
(def tuesday #date "2019-09-17")
(def wednesday #date "2019-09-18")
(def thursday #date "2019-09-19")
(def friday #date "2019-09-20")
(def saturday #date "2019-09-21")
(def sunday #date "2019-09-22")

(s/with-fn-validation
  (tabular
   (fact "last-day-of-month?"
     (time/last-day-of-month? (from-string ?date)) => ?last-day-of-month)
   ?date              ?last-day-of-month
   "2019-08-30"       false
   "2019-08-31"       true
   "2019-09-01"       false
   "2019-09-30"       true
   "2019-02-28"       true
   "2020-02-28"       false
   "2020-02-29"       true)

  (tabular
   (fact "friday?"
     (time/friday? ?date) => ?friday)
   ?date       ?friday
   monday      false
   tuesday     false
   wednesday   false
   thursday    false
   friday      true
   saturday    false
   sunday      false)) 
