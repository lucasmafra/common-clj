(ns common-clj.schemata.http-server
  (:require [schema.core :as s]))

(def RouteSettings
  {:path                                s/Str
   :method                              (s/enum :get :put :post :delete :options)
   :handler                             (s/pred clojure.test/function?)
   (s/optional-key :request-schema)     {s/Any s/Any}
   (s/optional-key :path-params-schema) {s/Any s/Any}
   :response-schema                     {s/Any s/Any}})

(def Routes
  {s/Keyword RouteSettings})
