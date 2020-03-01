(ns common-clj.http-client.interceptors.json-serializer
  (:require [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :as json]
            [common-clj.misc :as misc]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(def default-serialization-map json/default-serialization-map)

(def default-serialize-fn json/json->string)

(def default-values
  {:serialize-fn default-serialize-fn})

(def json-serializer
  (interceptor/interceptor
   {:name  ::json-serializer
    :enter (fn [{:keys [endpoints endpoint] {:keys [body]} :options :as context}]
             (let [{:keys [request-schema]} (endpoints endpoint)
                   request-schema           (or request-schema cs/Empty)
                   {:keys [serialize-fn]}   (parse-overrides context :json-serializer default-values)
                   extension                (parse-overrides context :extend-serialization nil)
                   serialization-map        (merge default-serialization-map extension)
                   serialize-options        {:serialization-map serialization-map
                                             :transform-fns     [misc/dash->underscore]}
                   serialized               (when body (serialize-fn body request-schema serialize-options))]
               (assoc-in context [:options :body] serialized)))}))
