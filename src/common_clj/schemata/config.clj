(ns common-clj.schemata.config
  (:require [common-clj.schema-helpers :as csh]
            [schema.core :as s]))

(def dynamo-table s/Keyword)
(def key-name s/Keyword)
(def key-type (s/enum :s :n :ss :ns :b :bs))

(def DynamoDefinition
  {dynamo-table {:primary-key                    [(s/one key-name "key-name")
                                                  (s/one key-type "key-type")]

                 (s/optional-key :secondary-key) [(s/one key-name "key-name")
                                                  (s/one key-type "key-type")]}})

(def AppConfig
  (csh/loose-schema
   {:app-name                         s/Keyword
    (s/optional-key :kafka-server)    s/Str
    (s/optional-key :dynamo-endpoint) s/Str
    (s/optional-key :aws-access-key)  s/Str
    (s/optional-key :aws-secret-key)  s/Str
    (s/optional-key :http-port)       s/Int
    (s/optional-key :known-hosts)     {s/Keyword s/Str}
    (s/optional-key :dynamo-tables)   DynamoDefinition}))

(def Env
  (s/enum :test :dev :prod))
