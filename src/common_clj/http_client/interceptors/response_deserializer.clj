(ns common-clj.http-client.interceptors.response-deserializer
  (:require [cheshire.core :refer [parse-string]]
            [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(def default-parse-key-fn true)

(defn default-deserialize-fns [parse-key-fn]
  [misc/camelcase->dash
   misc/underscore->dash
   #(parse-string % parse-key-fn)])

(def default-values
  {:parse-key-fn    default-parse-key-fn
   :deserialize-fns default-deserialize-fns})

(def response-deserializer
  (interceptor/interceptor
   {:name  ::response-deserializer
    :leave (fn [{{:keys [body]} :response :keys [options overrides] :as context}]
             (let [{:keys [parse-key-fn deserialize-fns]} (parse-overrides context
                                                                           :response-deserializer
                                                                           default-values)
                   deserialized    ((apply comp (deserialize-fns parse-key-fn)) body)]
               (assoc-in context [:response :body] deserialized)))}))
