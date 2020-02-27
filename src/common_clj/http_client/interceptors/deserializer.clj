(ns common-clj.http-client.interceptors.deserializer
  (:require [cheshire.core :refer [parse-string]]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(defn- special-keys-fn [special-keys]
  (fn [s] (or (special-keys s) (keyword s))))

(defn default-deserialize-fns [parse-key-fn]
  [misc/camelcase->dash
   misc/underscore->dash
   #(parse-string % parse-key-fn)])

(def default-values
  {:parse-key-fn    true
   :deserialize-fns default-deserialize-fns
   :special-keys    nil})

(def deserializer
  (interceptor/interceptor
   {:name  ::deserializer
    :leave (fn [{{:keys [body]} :response :keys [options overrides] :as context}]
             (let [{:keys [parse-key-fn deserialize-fns special-keys]} (parse-overrides context :deserializer default-values)
                   parse-key-fn (if special-keys (special-keys-fn special-keys) parse-key-fn)
                   deserialized    ((apply comp (deserialize-fns parse-key-fn)) body)]
               (assoc-in context [:response :body] deserialized)))}))
