(ns common-clj.kafka.consumer.interceptors.json-deserializer
  (:require [cheshire.core :refer [parse-string]]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(defn- special-keys-fn [special-keys]
  (fn [s] (or (special-keys s) (keyword s))))

(defn default-deserialize-fn
  ([s] (default-deserialize-fn s {}))
  ([s extension-map]
   (-> s
       (parse-string (special-keys-fn extension-map))
       misc/underscore->dash
       misc/camelcase->dash)))

(def default-values
  {:deserialize-fn         default-deserialize-fn
   :extend-deserialization nil})

(defn parse-overrides [{:keys [overrides]} k default]
  (merge default (k overrides)))

(def json-deserializer
  (interceptor/interceptor
   {:name  ::json-deserializer
    :enter (fn [{:keys [record] :as context}]
             (let [{:keys [deserialize-fn extend-deserialization]} (parse-overrides context :json-deserializer default-values)
                   deserialize-fn (if (nil? extend-deserialization) deserialize-fn #(deserialize-fn % extend-deserialization))
                   deserialized    (deserialize-fn (.value record))]
               (assoc context :message deserialized)))}))
