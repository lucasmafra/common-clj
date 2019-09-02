(ns common-clj.schemata.config
  (:require [schema.core :as s]))

(def AppConfig
  {:app-name                         s/Keyword
   (s/optional-key :kafka-server)    s/Str
   (s/optional-key :dynamo-endpoint) s/Str
   (s/optional-key :aws-access-key)  s/Str
   (s/optional-key :aws-secret-key)  s/Str
   (s/optional-key :http-port)       s/Int})

(def Env
  (s/enum :test :dev :prod))
