(ns user
  (:require clojure.tools.namespace.repl
            [com.stuartsierra.component.repl :as repl :refer [reset set-init]]
            [java-time :refer [instant local-date local-date-time local-time]]))

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

(defmethod print-method java.time.LocalTime
  [dt out]
  (.write out (str "#local-time \"" (str dt) "\"")))

(defmethod print-dup java.time.LocalTime
  [dt out]
  (.write out (str "#local-time \"" (str dt) "\"")))

(defmethod print-method java.time.Instant
  [dt out]
  (.write out (str "#instant \"" (str dt) "\"")))

(defmethod print-dup java.time.Instant
  [dt out]
  (.write out (str "#instant \"" (str dt) "\"")))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")
