(ns common-clj.http-client.interceptors.with-mock-calls
  (:require [clj-http.fake :refer [with-fake-routes]]
            [clojure.test :refer [function?]]
            [common-clj.config.protocol :as config-pro]
            [common-clj.json :as json]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]
            [selmer.parser :refer [known-variables render]]))

(def ^:dynamic *mock-calls* {})

(defn mock-calls! [value]
  (alter-var-root #'*mock-calls* (constantly value)))

(defn- fake-host [variable]
  (str "http://" (name variable) ".com"))

(defn- extract-host-variables [mock-calls]
  (->> mock-calls keys (filter string?) (map known-variables) (map first) (remove nil?) set))

(defn- replace-url [template variable]
  (render template {variable (fake-host variable)}))

(defn- transform-body [mock {:keys [options] :as context}]
  (misc/map-vals
   (fn [{:keys [body status] :as v}]
     (cond
       (function? v) (let [{:keys [status body]} (v options)]
                       (constantly {:status status :body (if (string? body)
                                                           body
                                                           (json/json->string body))}))
       (empty? v) {}
       (some? body) (constantly {:status status :body (if (string? body)
                                                        body
                                                        (json/json->string body))})
       :else        (transform-body v context)))
   mock))

(defn- transform-hosts [mock]
  (misc/map-keys
   (fn [k]
     (if (string? k)
       (if-let [variable (first (known-variables k))]
         (replace-url k variable)
         k)
       k))
   mock))

(defn ^:private transform-mock [mock-calls context]
  (-> mock-calls transform-hosts (transform-body context)))

(defn- wrap-handler-interceptor
  "Wraps handler interceptor into with-fake-routes"
  [queue mock-calls]
  (->> queue
       seq
       (map (fn [{:keys [name enter] :as interceptor}]
              (if (= :common-clj.http-client.interceptors.handler/handler name)
                (assoc interceptor :enter (fn [context] (with-fake-routes
                                                          (transform-mock mock-calls context)
                                                          (enter context))))
                interceptor)))
       (into clojure.lang.PersistentQueue/EMPTY)))

(defn- mock-known-hosts! [hosts config]
  (doseq [host hosts]
    (config-pro/assoc-in! config [:known-hosts host] (fake-host host))))

(defn with-mock-calls
  ([] (with-mock-calls nil))
  ([mock-calls]
   (interceptor/interceptor
    {:name ::with-mock-calls
     :enter (fn [{:keys [:io.pedestal.interceptor.chain/queue] {:keys [config]} :components :as context}]
              (let [mock-calls (or mock-calls *mock-calls* {})
                    host-variables (extract-host-variables mock-calls)
                    modified-queue (wrap-handler-interceptor queue mock-calls)]
                (mock-known-hosts! host-variables config)
                (assoc context :io.pedestal.interceptor.chain/queue modified-queue)))})))
