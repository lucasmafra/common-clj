(ns common-clj.schema
  (:require [java-time :as time]
            [schema.core :as s]))

(def LocalDate
  "Date without timezone information. Useful for stuff such as date of birth, hollidays and so on. 
   Ex: 2001-12-09"
  java.time.LocalDate)

(def LocalDateTime
  "Same as LocalDate, but with additional time information. Can't think of a useful use case.
   Ex: 2001-12-09T09:00:00"
  java.time.LocalDateTime)

(def LocalTime
  "Time of the day, no timezone information.
   Ex: 13:12:09"
  java.time.LocalTime)

(def UTCDateTime
  "ISO 8601 string representing a UTC date time.
   Ex: 2001-12-09T09:00:00Z"
  (s/pred (partial instance? java.time.Instant) 'utc-date-time))

(s/defschema EpochMillis
  "Unix epoch in milliseconds - the number of milliseconds elapsed since 1970-01-01T00:00:00Z.
   Ex: 1581797873000"
  (s/pred (partial instance? java.time.Instant) 'epoch-millis))

(def TimestampMicroseconds
  "Unix timestamp in microseconds. Ex: 1582769681000000"
  (s/pred pos-int? 'timestamp-microseconds))

(def PosInt
  (s/pred pos-int? 'pos-int))

(def email-regex
  #"^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")

(def Email
  (s/pred (partial re-matches email-regex) 'valid-email))

(def Nil (s/pred nil? 'nil?))

(def Empty (s/pred empty? 'empty?))
