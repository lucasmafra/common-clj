(ns common-clj.config.schemata
  (:require [common-clj.schema-helpers :as csh]
            [schema.core :as s]))

(def AppConfig
  (csh/loose-schema
   {:app-name                     s/Keyword
    (s/optional-key :http-port)   s/Int
    (s/optional-key :known-hosts) {s/Keyword s/Str}}))

(def Env
  (s/enum :test :dev :prod))
