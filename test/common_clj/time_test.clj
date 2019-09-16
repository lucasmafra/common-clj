(ns common-clj.time-test
  (:require [midje.sweet :refer :all]
            [common-clj.time :as time]
            [schema.core :as s])
  (:import java.time.LocalDate))

(defn from-string [date] (LocalDate/parse date))

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
   "2020-02-29"       true))
