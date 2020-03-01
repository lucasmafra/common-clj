(ns common-clj.http-client.interceptors.with-mock-calls
  (:require [clj-http.fake :refer [with-fake-routes]]
            [common-clj.components.config.protocol :as config-pro]
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

(defn- transform-body [mock]
  (misc/map-vals
   (fn [{:keys [body status] :as val}]
     (if (empty? val)
       {}
       (if body
         (constantly {:status status :body (json/json->string body)})
         (transform-body val))))
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

(def ^:private transform-mock (comp transform-body transform-hosts))

(defn- wrap-handler-interceptor
  "Wraps handler interceptor into with-fake-routes"
  [queue mock-calls]
  (->> queue
       seq
       (map (fn [{:keys [name enter] :as interceptor}]
              (if (= :common-clj.http-client.interceptors.handler/handler
                     name)
                (assoc interceptor :enter (fn [context] (with-fake-routes (transform-mock mock-calls)
                                                          (enter context))))
                interceptor)))
       (into clojure.lang.PersistentQueue/EMPTY)))

(defn- mock-known-hosts! [hosts config]
  (doseq [host hosts]
    (config-pro/assoc-in! config [:known-hosts host] (fake-host host))))

(def with-mock-calls
  (interceptor/interceptor
   {:name ::with-mock-calls
    :enter (fn [{:keys [:io.pedestal.interceptor.chain/queue] {:keys [config]} :components :as context}]
             (let [mock-calls (or *mock-calls* {})
                   host-variables (extract-host-variables mock-calls)
                   modified-queue (wrap-handler-interceptor queue mock-calls)]
               (mock-known-hosts! host-variables config)
               (assoc context :io.pedestal.interceptor.chain/queue modified-queue)))}))
