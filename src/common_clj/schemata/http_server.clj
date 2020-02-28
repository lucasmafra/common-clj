(ns common-clj.schemata.http-server
  (:require [schema.core :as s]))

(def RouteSettings
  {:path                                 s/Str
   :method                               (s/enum :get :put :post :delete :options)
   :handler                              s/Any
   (s/optional-key :request-schema)      s/Any
   (s/optional-key :path-params-schema)  s/Any
   (s/optional-key :query-params-schema) s/Any
   :response-schema                      s/Any})

(def BodyCoercerOverrides
  {(s/optional-key :coercers) s/Any
   (s/optional-key :extension) s/Any})

(def PathParamsCoercerOverrides
  {(s/optional-key :coercers) s/Any
   (s/optional-key :extension) s/Any})

(def QueryParamsCoercerOverrides
  {(s/optional-key :coercers) s/Any
   (s/optional-key :extension) s/Any})

(def JsonSerializerOverrides
  {(s/optional-key :serialize-fn) s/Any})

(def Overrides
  {(s/optional-key :body-coercer)         BodyCoercerOverrides
   (s/optional-key :path-params-coercer)  PathParamsCoercerOverrides
   (s/optional-key :query-params-coercer) QueryParamsCoercerOverrides
   (s/optional-key :service-map)          s/Any
   (s/optional-key :interceptors-fn)      s/Any
   (s/optional-key :extend-serialization) s/Any
   (s/optional-key :json-serializer)      JsonSerializerOverrides})

(def Routes
  {s/Keyword RouteSettings})
