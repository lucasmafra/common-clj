(ns common-clj.http-client.interceptors.with-mock-calls
  (:require [io.pedestal.interceptor :as interceptor]
            [clj-http.fake :refer [with-fake-routes]]
            [io.pedestal.interceptor.chain :as chain]))

(def ^:dynamic *mock-calls* {})

(defn mock-calls! [value]
  (alter-var-root #'*mock-calls* (constantly value)))


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
                                                                       (with-fake-routes (or *mock-calls* {})
                                                                         (enter context))))
                                           interceptor)))
                                  (into clojure.lang.PersistentQueue/EMPTY))]
               (assoc context :io.pedestal.interceptor.chain/queue new-queue)))}))