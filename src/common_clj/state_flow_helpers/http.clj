(ns common-clj.state-flow-helpers.http
  (:require [common-clj.json :as json]
            [io.pedestal.test :as test]
            [state-flow.state :as state]))

(defn- prettify-body [body]
  (try
    (json/string->json body)
    (catch Exception e
      body)))

(defn request-arrived! [method path]
  (state/gets
   (fn [world]
     (let [service-fn (or (-> world :system :http-server :service :io.pedestal.http/service-fn)
                          (throw (AssertionError. "No http server found in the system")))
           {:keys [status body]} (test/response-for service-fn method path)]
       {:status status
        :body   (prettify-body body)}))))
