(ns common-clj.http-server.interceptors.query-params-coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-server.interceptors.helpers :refer [parse-overrides]]
            [common-clj.schema.core :as cs]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers  default-coercers
   :extension nil})

(def query-params-coercer
  (interceptor/interceptor
   {:name  ::query-params-coercer
    :enter
    (fn [{:keys [request route routes] :as context}]
      (let [{:keys [query-params]}        request
            {:keys [route-name]}          route
            {:keys [query-params-schema]} (route-name routes)
            query-params-schema           (or query-params-schema cs/Empty)
            {:keys [coercers extension]}  (parse-overrides context :query-params-coercer default-values)
            coercers                      (merge coercers extension)
            coerced-query-params          (coercion/coerce query-params-schema query-params coercers)]
        (assoc-in context [:request :query-params] coerced-query-params)))}))
