(ns common-clj.schemata.http-client
  (:require [schema.core :as s]))

(def JsonSerializerOverrides
  {(s/optional-key :serialize-fn) s/Any})

(def JsonDeserializerOverrides
  {(s/optional-key :deserialize-fn) [s/Any]})

(def CoercerOverrides
  {(s/optional-key :coercers) [s/Any]})

(def Overrides
  {(s/optional-key :json-serializer)        JsonSerializerOverrides
   (s/optional-key :json-deserializer)      JsonDeserializerOverrides
   (s/optional-key :coercer)                CoercerOverrides
   (s/optional-key :extend-serialization)   s/Any
   (s/optional-key :extend-deserialization) s/Any
   (s/optional-key :extend-coercion)        s/Any})

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
