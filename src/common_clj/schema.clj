(ns common-clj.schema
  (:require [schema.core :as s]
            [java-time :as time]))

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
  java.time.Instant)

(def EpochMillis
  "Unix epoch in milliseconds - the number of milliseconds elapsed since 1970-01-01T00:00:00Z.
   Ex: 1581797873000"
  java.time.Instant)

(def PosInt
  (s/pred pos-int?))

(def Email)
