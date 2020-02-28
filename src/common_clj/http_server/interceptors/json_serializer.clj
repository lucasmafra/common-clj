(ns common-clj.http-server.interceptors.json-serializer
  (:require [common-clj.http-server.interceptors.helpers :refer [parse-overrides]]
            [common-clj.json :as json->string :refer [json->string]]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]
            [common-clj.json :as json]))

(def default-serialization-map json->string/default-serialization-map)

(defn default-serialize-fn [json schema serialization-map]
  (-> json
      misc/dash->underscore
      (json/json->string schema serialization-map)))

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
            _                         (s/validate response-schema body)
            {:keys [serialize-fn]}    (parse-overrides context :json-serializer default-values)
            extension                 (parse-overrides context :extend-serialization nil)
            serialization-map         (merge default-serialization-map extension)
            serialized-body           (serialize-fn body response-schema serialization-map)]
        
        (assoc-in context [:response :body] serialized-body)))}))
