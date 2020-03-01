(ns aux.http-server
  (:require [common-clj.json :as json]
            [io.pedestal.test :as test]
            [state-flow.state :as state]))

(defn- prettify-body [body]
  (try
    (json/string->json body false)
    (catch Exception _
      body)))

(defn request-arrived<no-print>! [method path & options]
  (state/gets
   (fn [world]
     (let [service-fn (or (-> world :system :http-server :service-map :io.pedestal.http/service-fn)
                          (throw (AssertionError. "No http server found in the system")))
           {:keys [status body]} (with-redefs [println (constantly nil)]
                                   (apply test/response-for service-fn method path options))]
       {:status status
        :body   (prettify-body body)}))))

(def GET<no-print> (partial request-arrived<no-print>! :get))
(def POST<no-print> (partial request-arrived<no-print>! :post))
(def PUT<no-print> (partial request-arrived<no-print>! :put))
(def DELETE<no-print> (partial request-arrived<no-print>! :delete))
(def OPTIONS<no-print> (partial request-arrived<no-print>! :options))
(def HEAD<no-print> (partial request-arrived<no-print>! :head))
