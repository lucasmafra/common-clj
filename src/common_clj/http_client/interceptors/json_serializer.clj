(ns common-clj.http-client.interceptors.json-serializer
  (:require [common-clj.http-client.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :as json]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]
            [common-clj.misc :as misc]))

(def default-serialization-map json/default-serialization-map)

(defn default-serialize-fn
  ([json]
   (default-serialize-fn json default-serialization-map))
  ([json serialization-map]
   (-> json
       misc/dash->underscore
       (json/json->string serialization-map))))

(def default-values
  {:serialize-fn default-serialize-fn
   :extension    nil})

(def json-serializer
  (interceptor/interceptor
   {:name  ::json-serializer
    :enter (fn [{:keys [endpoints endpoint] {:keys [body]} :options :as context}]
             (let [{:keys [request-schema]}         (endpoints endpoint)
                   {:keys [serialize-fn extension]} (parse-overrides context :json-serializer default-values)
                   serialization-map                (merge default-serialization-map extension)
                   serialize-fn (if (nil? extension) serialize-fn #(serialize-fn % serialization-map))]
               (when (and (not request-schema) body)
                 (throw (AssertionError. "Body is present on request but there's no request-schema for endpoint " endpoint)))
               (if request-schema
                 (do
                   (s/validate request-schema body)
                   (assoc-in context [:options :body] (serialize-fn body)))
                 context)))}))
