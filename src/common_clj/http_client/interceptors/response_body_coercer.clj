(ns common-clj.http-client.interceptors.response-body-coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.json :refer [string->json]]
            [io.pedestal.interceptor :as interceptor]))

(def response-body-coercer
  (interceptor/interceptor
   {:name  ::response-body-coercer
    :leave (fn [{{:keys [endpoint]} :request :keys [coercers endpoints] {:keys [body]} :response :as context}]
             (let [{:keys [response-schema]} (endpoints endpoint)
                   coerced-body              (coercion/coerce response-schema body coercers)]
               (assoc-in context [:response :body] coerced-body)))}))
