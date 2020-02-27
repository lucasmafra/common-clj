(ns common-clj.http-client.interceptors.json-deserializer
  (:require [cheshire.core :refer [parse-string]]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(defn- special-keys-fn [special-keys]
  (fn [s] (or (special-keys s) (keyword s))))

(defn default-deserialize-fn
  ([string]
   (default-deserialize-fn string {}))
  ([string extension-map]
   (-> string
       (parse-string (special-keys-fn extension-map))
       misc/underscore->dash
       misc/camelcase->dash)))

(def default-values
  {:deserialize-fn default-deserialize-fn
   :extension      nil})

(def json-deserializer
  (interceptor/interceptor
   {:name  ::json-deserializer
    :leave (fn [{{:keys [body]} :response :as context}]
             (let [{:keys [deserialize-fn]} (parse-overrides context :json-deserializer default-values)
                   extension (parse-overrides context :extend-deserialization nil)
                   deserialize-fn (if (nil? extension) deserialize-fn #(deserialize-fn % extension))
                   deserialized    (deserialize-fn body)]
               (assoc-in context [:response :body] deserialized)))}))
