(ns common-clj.http-server.interceptors.content-type
  (:require [io.pedestal.interceptor :as interceptor]))

(def content-type
  (interceptor/interceptor
   {:name ::content-type
    :leave (fn [context]
             (assoc-in context
                       [:response :headers]
                       {"Content-Type" "application/json"}))}))
