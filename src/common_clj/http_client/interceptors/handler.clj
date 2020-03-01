(ns common-clj.http-client.interceptors.handler
  (:require [clj-http.client :as client]
            [io.pedestal.interceptor :as interceptor]))

(def handler
  (interceptor/interceptor
   {:name ::handler
    :enter (fn [{:keys [endpoint endpoints url options] :as context}]
             (let [{:keys [method]} (endpoints endpoint)
                   response (case method
                              :get     (client/get url options)
                              :post    (client/post url options)
                              :put     (client/put url options)
                              :delete  (client/delete url options)
                              :options (client/options url options)
                              :head    (client/head url options))]
               (assoc context :response response)))}))
