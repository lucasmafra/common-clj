(ns user
  (:require [java-time :refer [local-date local-date-time]]))

;; Configure the printer
(defmethod print-method java.time.LocalDate
  [dt out]
  (.write out (str "#date \"" (str dt) "\"")))

(defmethod print-dup java.time.LocalDate
  [dt out]
  (.write out (str "#date \"" (str dt) "\"")))

(defmethod print-method java.time.LocalDateTime
  [dt out]
  (.write out (str "#date-time \"" (str dt) "\"")))

(defmethod print-dup java.time.LocalDateTime
  [dt out]
  (.write out (str "#date-time \"" (str dt) "\"")))
