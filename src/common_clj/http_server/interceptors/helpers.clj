(ns common-clj.http-server.interceptors.helpers)

(defn parse-overrides [{{:keys [route-name]} :route :as context} k default-value]
  (let [route-override     (-> context :routes route-name :overrides k)
        component-override (-> context :overrides k)]
    (merge default-value
           component-override
           route-override)))
