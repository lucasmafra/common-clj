(ns common-clj.kafka.producer.interceptors.json-serializer
  (:require [common-clj.json :as json]
            [common-clj.misc :as misc]
            [io.pedestal.interceptor :as interceptor]))

(def default-values
  {:serialize-fn         json/json->string
   :transform-fns        [misc/dash->underscore]
   :serialization-map    json/default-serialization-map
   :extend-serialization nil})

(defn parse-overrides [{:keys [overrides]} k default]
  (merge default (k overrides)))

(def json-serializer
  (interceptor/interceptor
   {:name  ::json-serializer
    :enter (fn [{:keys [topics topic message] :as context}]
             (let [{:keys [schema]}      (topics topic)
                   {:keys [serialize-fn
                           transform-fns
                           serialization-map
                           extend-serialization]}
                   (parse-overrides context :json-serializer default-values)
                   serialization-options {:transform-fns     transform-fns
                                          :serialization-map (merge serialization-map
                                                                    extend-serialization)}]
               (assert (some? schema) (str "Missing schema for topic " topic))
               (assoc context :message (serialize-fn message schema serialization-options))))}))
