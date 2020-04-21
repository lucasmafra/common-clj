(ns common-clj.config.schemata
  (:require [common-clj.schema.helpers :as csh]
            [schema.core :as s]))

(def AppConfig
  (csh/loose-schema
   {:app/name                     s/Keyword}))

(def Env
  (s/enum :test :dev :prod))
