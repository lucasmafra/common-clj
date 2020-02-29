(ns common-clj.state-flow-helpers.http-server
  (:require [common-clj.json :as json]
            [io.pedestal.test :as test]
            [state-flow.state :as state]))

(defn- prettify-body [body]
  (try
    (json/string->json body false)
    (catch Exception e
      body)))

(defn request-arrived! [method path & options]
  (state/gets
   (fn [world]
     (let [service-fn (or (-> world :system :http-server :service :io.pedestal.http/service-fn)
                          (throw (AssertionError. "No http server found in the system")))
           {:keys [status body]} (apply test/response-for service-fn method path options)]
       {:status status
        :body   (prettify-body body)}))))

(def GET (partial request-arrived! :get))
(def POST (partial request-arrived! :post))
(def PUT (partial request-arrived! :put))
(def DELETE (partial request-arrived! :delete))
(def OPTIONS (partial request-arrived! :options))
(def HEAD (partial request-arrived! :head))
