(ns common-clj.http-server.interceptors.context-initializer
  (:require [io.pedestal.interceptor :as interceptor]))

(defn context-initializer [routes env overrides]
  (interceptor/interceptor
   {:name ::context-initializer
    :enter (fn [context]
             (-> context
                 (assoc :routes routes)
                 (assoc :env env)
                 (assoc :overrides overrides)))}))
