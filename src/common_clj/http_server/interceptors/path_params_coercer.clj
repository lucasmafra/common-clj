(ns common-clj.http-server.interceptors.path-params-coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.http-server.interceptors.helpers :refer [parse-overrides]]
            [common-clj.schema.core :as cs]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers  default-coercers
   :extension nil})

(def path-params-coercer
  (interceptor/interceptor
   {:name  ::path-params-coercer
    :enter
    (fn [{:keys [request route routes] :as context}]
      (let [{:keys [path-params]}        request
            {:keys [route-name]}         route
            {:keys [path-params-schema]} (route-name routes)
            path-params-schema           (or path-params-schema cs/Empty)
            {:keys [coercers extension]} (parse-overrides context :path-params-coercer default-values)
            coercers                     (merge coercers extension)
            coerced-path-params          (coercion/coerce path-params-schema path-params coercers)]
        (assoc-in context [:request :path-params] coerced-path-params)))}))
