(ns common-clj.http-client.interceptors.coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers default-coercers})

(def coercer
  (interceptor/interceptor
   {:name  ::coercer
    :leave (fn [{:keys [endpoints endpoint] {:keys [body]} :response :as context}]
             (let [{:keys [response-schema]} (endpoints endpoint)
                   {:keys [coercers]}        (parse-overrides context :coercer default-values)
                   extension                 (parse-overrides context :extend-coercion nil)
                   coercers                  (merge coercers extension)
                   coerced-body              (coercion/coerce response-schema body coercers)]
               (assoc-in context [:response :body] coerced-body)))}))
