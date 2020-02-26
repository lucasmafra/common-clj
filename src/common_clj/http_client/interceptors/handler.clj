(ns common-clj.http-client.interceptors.handler
  (:require [io.pedestal.interceptor :as interceptor]
            [clj-http.client :as client]))

(def handler
  (interceptor/interceptor
   {:name ::handler
    :enter (fn [{:keys [endpoints] {:keys [endpoint url options]} :request :as context}]
             (let [{:keys [method]} (endpoints endpoint)
                   response (case method
                              :get     (client/get url options)
                              :post    (client/post url options)
                              :put     (client/put url options)
                              :delete  (client/delete url options)
                              :options (client/options url options)
                              :head    (client/head url options))]
               (assoc context :response response)))}))
