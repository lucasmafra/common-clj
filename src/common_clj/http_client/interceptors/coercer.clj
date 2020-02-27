(ns common-clj.http-client.interceptors.coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :refer [string->json]]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers default-coercers
   :extension nil})

(def coercer
  (interceptor/interceptor
   {:name  ::coercer
    :leave (fn [{:keys [endpoints endpoint] {:keys [body]} :response :as context}]
             (let [{:keys [response-schema]}    (endpoints endpoint)
                   {:keys [coercers extension]} (parse-overrides context :coercer default-values)
                   coercers                     (merge coercers extension)
                   coerced-body                 (coercion/coerce response-schema body coercers)]
               (assoc-in context [:response :body] coerced-body)))}))
