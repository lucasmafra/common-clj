(ns common-clj.http-client.interceptors.request-body
  (:require [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(def request-body
  (interceptor/interceptor
   {:name ::request-body
    :enter (fn [{:keys [endpoints] {{:keys [body]} :options :keys [endpoint]} :request :as context}]
             (let [{:keys [request-schema]} (endpoints endpoint)]
               (when request-schema
                 (s/validate request-schema body))
               (when (and (not request-schema) body)
                 (throw (AssertionError. "Body is present on request but there's no request-schema for endpoint " endpoint)))
               context))}))
