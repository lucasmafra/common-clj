(ns common-clj.http-client.interceptors.context-initializer
  (:require [io.pedestal.interceptor :as interceptor]))

(defn context-initializer [{:keys [endpoints endpoint options config coercers mock-http-client-calls]}]
  (interceptor/interceptor
   {:name ::context-initializer
    :enter (fn [context]
             (-> context
                 (assoc-in [:endpoints] endpoints)
                 (assoc-in [:request :endpoint] endpoint)
                 (assoc-in [:request :options] options)
                 (assoc-in [:config] config)
                 (assoc-in [:coercers] coercers)
                 (assoc-in [:mock-http-client-calls] mock-http-client-calls)))}))
