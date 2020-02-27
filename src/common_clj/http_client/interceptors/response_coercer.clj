(ns common-clj.http-client.interceptors.response-coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :refer [string->json]]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercers)

(def default-values
  {:coercers default-coercers})

(def response-coercer
  (interceptor/interceptor
   {:name  ::response-coercer
    :leave (fn [{{:keys [endpoint]} :request :keys [endpoints] {:keys [body]} :response :as context}]
             (let [{:keys [response-schema]} (endpoints endpoint)
                   {:keys [coercers]} (parse-overrides context :response-coercer default-values)
                   coerced-body              (coercion/coerce response-schema body coercers)]
               (assoc-in context [:response :body] coerced-body)))}))
