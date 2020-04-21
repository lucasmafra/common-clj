(ns common-clj.http-server.interceptors.json-serializer
  (:require [common-clj.http-server.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :as json]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(def default-serialization-map json/default-serialization-map)

(def default-serialize-fn json/json->string)

(def default-values
  {:serialize-fn default-serialize-fn
   :extension nil})

(def json-serializer
  (interceptor/interceptor
   {:name ::json-serializer
    :leave
    (fn [{:keys [response routes route] :as context}]
      (let [{:keys [body]}            response
            {:keys [route-name]}      route
            {:keys [response-schema]} (route-name routes)
            {:keys [serialize-fn]}    (parse-overrides context :json-serializer default-values)
            extension                 (parse-overrides context :extend-serialization nil)
            serialization-map         (merge default-serialization-map extension)
            serialize-options         {:serialization-map serialization-map
                                       :transform-fns     [misc/dash->underscore]}
            serialized-body           (serialize-fn body response-schema serialize-options)]

        (assoc-in context [:response :body] serialized-body)))}))
