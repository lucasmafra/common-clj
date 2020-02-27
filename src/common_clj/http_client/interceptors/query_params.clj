(ns common-clj.http-client.interceptors.query-params
  (:require [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(def query-params
  (interceptor/interceptor
   {:name ::query-params
    :enter (fn [{:keys [endpoints endpoint] {:keys [query-params]} :options :as context}]
             (let [{:keys [query-params-schema]} (endpoints endpoint)]
               (when query-params-schema
                 (s/validate query-params-schema query-params))
               (when (and (not query-params-schema) query-params)
                 (throw (AssertionError. ":query-params is present on request but there's no query-params-schema for endpoint " endpoint)))
               context))}))
