(ns common-clj.kafka.consumer.interceptors.coercer
  (:require [common-clj.coercion :as coercion]
            [common-clj.kafka.consumer.interceptors.helpers :refer [match-topic? parse-overrides]]
            [io.pedestal.interceptor :as interceptor]))

(def default-coercers coercion/default-coercion-map)

(def default-values
  {:coercers default-coercers})

(def coercer
  (interceptor/interceptor
   {:name ::coercer
    :enter
    (fn [{:keys [topics message record] :as context}]
      (let [topic                              (ffirst (filter (match-topic? record) topics))
            {:keys [schema]}                   (topics topic)
            {:keys [coercers extend-coercion]} (parse-overrides context :coercer default-values)
            coercers                           (merge coercers extend-coercion)
            coerced-msg                        (coercion/coerce schema message coercers)]
        (assoc context :message coerced-msg)))}))
