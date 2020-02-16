(ns user
  (:require
   [com.stuartsierra.component.user-helpers :refer [dev go reset]]
   [midje.repl]
   [java-time :refer [local-date local-date-time]]))

;; Configure the printer
(defmethod print-method java.time.LocalDate
  [dt out]
  (.write out (str "#local-date \"" (str dt) "\"")))

(defmethod print-dup java.time.LocalDate
  [dt out]
  (.write out (str "#local-date \"" (str dt) "\"")))

(defmethod print-method java.time.LocalDateTime
  [dt out]
  (.write out (str "#local-date-time \"" (str dt) "\"")))

(defmethod print-dup java.time.LocalDateTime
  [dt out]
  (.write out (str "#local-date-time \"" (str dt) "\"")))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")
