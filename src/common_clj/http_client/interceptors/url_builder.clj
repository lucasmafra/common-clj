(ns common-clj.http-client.interceptors.url-builder
  (:require [common-clj.components.config.protocol :as config-protocol]
            [io.pedestal.interceptor :as interceptor]
            [selmer.parser :as sp]))

(def url-builder
  (interceptor/interceptor
   {:name  ::url-builder
    :enter (fn [{:keys [endpoints endpoint path-replaced] {:keys [config]} :components :as context}]
             (let [{:keys [host]}        (endpoints endpoint)
                   {:keys [known-hosts]} (config-protocol/get-config config)
                   url-template          (str host path-replaced)
                   url                   (sp/render url-template known-hosts)]
               (assoc context :url url)))}))
