(ns common-clj.schemata.http
  (:require [schema.core :as s]))

(def RouteSettings
  {:path                            s/Str
   :method                          (s/enum :get :put :post :delete :options)
   :handler                         (s/pred clojure.test/function?)
   (s/optional-key :request-schema) {s/Keyword s/Any}
   :response-schema                 {s/Keyword s/Any}})

(def Routes
  {s/Keyword RouteSettings})
