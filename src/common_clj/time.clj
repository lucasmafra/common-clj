(ns common-clj.time
  (:require [schema.core :as s]
            [java-time :as time])
  (:import java.time.LocalDate))

(s/defn last-day-of-month? :- s/Bool
  [date :- LocalDate]
  (let [last-day-of-month (time/adjust date :last-day-of-month)]
    (= last-day-of-month date)))

(s/defn friday? :- s/Bool
  [date :- LocalDate]
  (time/friday? date))

(def minus time/minus)
(def days time/days)
(def before? time/before?)
(def local-date-time->local-date time/local-date)
