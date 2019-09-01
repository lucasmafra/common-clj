(ns common-clj.lib.pedestal
  (:require [schema.core :as s]
            [common-clj.schemata.http :as schemata.http]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as interceptor]
            [schema.coerce :as coerce]))

(defn- coerce
  [schema message]
  (let [coercer (coerce/coercer schema coerce/json-coercion-matcher)]
    (coercer message)))

(s/defn routes->pedestal [routes :- schemata.http/Routes]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path method handler :route-name route-name]))
   routes))

(defn coerce-request
  [schema]
  (interceptor/interceptor
   {:name :echo
    :enter
    (fn [{:keys [json-params] :as context}]
      (assoc context :body (coerce schema json-params)))}))

(defn hello [request]
  {:status 200 :body (-> request :json-params :foo)})

(def routes
  #{["/hello" :post hello :route-name :hello]})

#_(def server
  (-> {::http/routes routes
       ::http/type      :jetty
       ::http/port      8080
       ::http/join?     false}
      http/default-interceptors
      (update ::http/interceptors conj (body-params))
      http/create-server))

#_(test/response-for (-> server ::http/service-fn)
                   :post "/hello"
                   :headers {"Content-Type" "application/json"}
                   :body "{\"foo\": \"bar\"}")
