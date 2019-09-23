(ns common-clj.schemata.http-client
  (:require [schema.core :as s]))

(def EndpointSettings
  {:path                                s/Str
   :method                              (s/enum :get :put :post :delete :options)
   (s/optional-key :request-schema)     {s/Keyword s/Any}
   (s/optional-key :path-params-schema) {s/Keyword s/Any}
   :response-schema                     {s/Keyword s/Any}})

(def Endpoints
  {s/Keyword EndpointSettings})
