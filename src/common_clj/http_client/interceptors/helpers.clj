(ns common-clj.http-client.interceptors.helpers)

(defn parse-overrides [{{:keys [endpoint]} :request :as context} k default-value]
  (let [one-time-override  (-> context :request :options :overrides k)
        endpoint-override  (-> context :endpoints endpoint :overrides k)
        component-override (-> context :overrides k)]
    (merge default-value
           component-override
           endpoint-override
           one-time-override)))
