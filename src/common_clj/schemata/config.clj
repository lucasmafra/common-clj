(ns common-clj.schemata.config
  (:require [schema.core :as s]))

(def AppConfig
  {:app-name s/Keyword
   :kafka-server s/Str})
