(ns common-clj.schemata.http-server
  (:require [schema.core :as s]))

(def RouteSettings
  {:route/path                          s/Str
   :route/method                        (s/enum :get :put :post :delete :options)
   :route/handler                       (s/pred clojure.test/function?)
   (s/optional-key :request/schema)     {s/Keyword s/Any}
   (s/optional-key :path-params/schema) {s/Keyword s/Any}
   :response/schema                     {s/Keyword s/Any}})

(def Routes
  {s/Keyword RouteSettings})
