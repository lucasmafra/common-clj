(ns common-clj.http-client.interceptors.url-builder
  (:require [io.pedestal.interceptor :as interceptor]
            [selmer.parser :as sp]))

(def url-builder
  (interceptor/interceptor
   {:name  ::url-builder
    :enter (fn [{:keys [endpoints] {:keys [endpoint path-replaced]} :request {:keys [known-hosts]} :config :as context}]
             (let [{:keys [host]} (endpoints endpoint)
                   url-template   (str host path-replaced)
                   url            (sp/render url-template known-hosts)]
               (assoc-in context [:request :url] url)))}))
