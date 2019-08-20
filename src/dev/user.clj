(ns user
  (:require [java-time :refer [local-date]]))

;; Configure the printer
(defmethod print-method java.time.LocalDate
  [dt out]
  (.write out (str "#date \"" (.toString dt) "\"")))

(defmethod print-dup java.time.LocalDate
  [dt out]
  (.write out (str "#date \"" (.toString dt) "\"")))
