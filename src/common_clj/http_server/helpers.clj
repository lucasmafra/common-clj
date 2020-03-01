(ns common-clj.http-server.helpers
  (:require [common-clj.http-server.default-interceptors :refer [default-handler-interceptors]]))

(defn- wrap-handler [handler components]
  (fn [request]
    (let [{:keys [status body headers]} (handler request components)]
      {:status  status
       :headers headers
       :body    body})))

(defn ->pedestal-routes [{:keys [routes components]}]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path
       method
       (conj default-handler-interceptors (wrap-handler handler components))
       :route-name route-name]))
   routes))
