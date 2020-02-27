(ns common-clj.schemata.http-client
  (:require [schema.core :as s]))

(def ResponseCoercerOverrides
  {(s/optional-key :coercers) [s/Any]
   (s/optional-key :extension) [s/Any]})

(def ResponseDeserializerOverrides
  {(s/optional-key :deserialize-fns) [s/Any]
   (s/optional-key :parse-key-fn)    s/Any
   (s/optional-key :special-keys)    s/Any})

(def Overrides
  {(s/optional-key :deserializer) ResponseDeserializerOverrides
   (s/optional-key :coercer)      ResponseCoercerOverrides})

(def EndpointSettings
  {:host                                 s/Str
   :path                                 s/Str
   :method                               (s/enum :get :put :post :delete :options :head)
   (s/optional-key :request-schema)      s/Any
   (s/optional-key :path-params-schema)  s/Any
   (s/optional-key :query-params-schema) s/Any
   :response-schema                      s/Any
   (s/optional-key :overrides)           Overrides})

(def Endpoints
  {s/Keyword EndpointSettings})
