(ns common-clj.http-server.interceptors.body-coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-server.interceptors.helpers :refer [parse-overrides]]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers default-coercers})

(def body-coercer
  (interceptor/interceptor
   {:name  ::body-coercer
    :enter (fn [{:keys [request route routes] :as context}]
             (let [{:keys [json-params]}    request
                   {:keys [route-name]}     route
                   {:keys [request-schema]} (route-name routes)
                   request-schema           (or request-schema cs/Nil)
                   {:keys [coercers]}       (parse-overrides context :coercer default-values)
                   extension                (parse-overrides context :extend-coercion nil)
                   coercers                 (merge coercers extension)
                   coerced-body             (coercion/coerce request-schema json-params coercers)]
               (assoc-in context [:request :body] coerced-body)))}))
