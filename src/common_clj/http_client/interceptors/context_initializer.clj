(ns common-clj.http-client.interceptors.context-initializer
  (:require [io.pedestal.interceptor :as interceptor]))

(defn context-initializer [{:keys [endpoints endpoint options known-hosts coercers overrides]}]
  (interceptor/interceptor
   {:name ::context-initializer
    :enter (fn [context]
             (-> context
                 (assoc-in [:endpoints] endpoints)
                 (assoc-in [:request :endpoint] endpoint)
                 (assoc-in [:request :options] options)
                 (assoc-in [:known-hosts] known-hosts)
                 (assoc-in [:overrides] overrides)))}))
