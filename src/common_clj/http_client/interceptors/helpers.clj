(ns common-clj.http-client.interceptors.helpers)

(defn parse-overrides [{:keys [endpoint] :as context} k default-value]
  (let [one-time-override  (-> context :options :overrides k)
        endpoint-override  (-> context :endpoints endpoint :overrides k)
        component-override (-> context :overrides k)]
    (merge default-value
           component-override
           endpoint-override
           one-time-override)))
