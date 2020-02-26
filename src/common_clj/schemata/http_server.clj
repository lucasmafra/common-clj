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

(def Overrides
  {(s/optional-key :override-coercers)     s/Any
   (s/optional-key :override-interceptors) s/Any
   (s/optional-key :override-humanizer)    s/Any})

(def Routes
  {s/Keyword RouteSettings})
