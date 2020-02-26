(ns common-clj.http-client.interceptors.with-mock-calls
  (:require [clj-http.fake :refer [with-fake-routes]]
            [common-clj.json :as json]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]))

(def ^:dynamic *mock-calls* {})

(defn mock-calls! [value]
  (alter-var-root #'*mock-calls* (constantly value)))

(defn map-vals
  "Map over the given hash-map vals.
  Example:
    (map-vals inc {:a 1 :b 2})
  "
  [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn transform-mock [mock]
  (map-vals
   (fn [{:keys [body status] :as val}]
     (if (empty? val)
       {}
       (if body
         (constantly {:status status :body (json/json->string body)})
         (transform-mock val))))
   mock))

(def with-mock-calls
  (interceptor/interceptor
   {:name ::with-mock-calls
    :enter (fn [{:keys [:io.pedestal.interceptor.chain/queue] :as context}]
             (let [new-queue (->> queue
                                  seq
                                  (map (fn [{:keys [name enter] :as interceptor}]
                                         (if (= :common-clj.http-client.interceptors.handler/handler
                                                name)
                                           (assoc interceptor :enter (fn [context]
                                                                       (with-fake-routes (transform-mock (or *mock-calls* {}))
                                                                         (enter context))))
                                           interceptor)))
                                  (into clojure.lang.PersistentQueue/EMPTY))]
               (assoc context :io.pedestal.interceptor.chain/queue new-queue)))}))
