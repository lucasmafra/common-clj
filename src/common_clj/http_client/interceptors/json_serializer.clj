(ns common-clj.http-client.interceptors.json-serializer
  (:require [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :as json]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]
            [common-clj.misc :as misc]
            [common-clj.schema :as cs]))

(def default-serialization-map json/default-serialization-map)

(defn default-serialize-fn
  [json schema serialization-map]
  (-> json
      misc/dash->underscore
      (json/json->string schema serialization-map)))

(def default-values
  {:serialize-fn default-serialize-fn})

(def json-serializer
  (interceptor/interceptor
   {:name  ::json-serializer
    :enter (fn [{:keys [endpoints endpoint] {:keys [body]} :options :as context}]
             (let [{:keys [request-schema]} (endpoints endpoint)
                   request-schema           (or request-schema cs/Empty)
                   _                        (s/validate request-schema body)
                   {:keys [serialize-fn]}   (parse-overrides context :json-serializer default-values)
                   extension                (parse-overrides context :extend-serialization nil)
                   serialization-map        (merge default-serialization-map extension)
                   serialized               (when body (serialize-fn body request-schema serialization-map))]
               (assoc-in context [:options :body] serialized)))}))
