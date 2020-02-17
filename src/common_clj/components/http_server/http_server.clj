(ns common-clj.components.http-server.http-server
  (:require [com.stuartsierra.component :as component]
            [common-clj.coercion :refer [coerce] :as coercion]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.http-server.protocol
             :as
             http-server.protocol
             :refer
             [HttpServer]]
            [common-clj.json :refer [json->string]]
            [common-clj.schemata.http-server :as schemata.http]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.error :as error-int]
            [schema.core :as s]
            [common-clj.lib.utils :refer [map-vals]]
            [common-clj.humanize :as humanize]
            [schema.utils :as s-utils])
  (:import (java.io StringWriter PrintWriter)))

(def default-coercers coercion/default-coercers)

(defn ok [body]
  {:status 200
   :body   body})

(defn wrap-handler [handler components]
  (fn [request]
    (let [{:keys [status body]} (handler request components)]
      {:status status
       :body   body})))

(def content-type
  (interceptor
   {:name ::content-type
    :leave (fn [context]
             (assoc-in context
                       [:response :headers]
                       {"Content-Type" "application/json"}))}))
(defn body-coercer
  [routes {:keys [override-coercers]}]
  (interceptor
   {:name  ::json-coercer
    :enter (fn [{:keys [request route] :as context}]
             (let [{:keys [json-params]}    request
                   {:keys [route-name]}     route
                   {:keys [request-schema]} (route-name routes)
                   coerced-body             (when request-schema
                                              (coerce request-schema json-params (or override-coercers default-coercers)))]
               (assoc-in context [:request :body] coerced-body)))
    :leave (fn [{:keys [response route] :as context}]
             (let [{:keys [body]}            response
                   {:keys [route-name]}      route
                   {:keys [response-schema]} (route-name routes)
                   coerced-body              (json->string body)]
               (s/validate response-schema body)
               (assoc-in context [:response :body] coerced-body)))}))

(defn path-params-coercer
  [routes {:keys [override-coercers]}]
  (interceptor
   {:name  ::path-params-coercer
    :enter (fn [{:keys [request route] :as context}]
             (let [{:keys [path-params]}        request
                   {:keys [route-name]}         route
                   {:keys [path-params-schema]} (route-name routes)
                   coerced-path-params (when path-params-schema
                                         (coerce path-params-schema path-params (or override-coercers default-coercers)))]
               (if coerced-path-params
                 (assoc-in context [:request :path-params] coerced-path-params)
                 context)))}))

(defn query-params-coercer
  [routes {:keys [override-coercers]}]
  (interceptor
   {:name  ::query-params-coercer
    :enter (fn [{:keys [request route] :as context}]
             (let [{:keys [query-params]}        request
                   {:keys [route-name]}          route
                   {:keys [query-params-schema]} (route-name routes)
                   coerced-query-params (when query-params-schema
                                         (coerce query-params-schema query-params (or override-coercers default-coercers)))]
               (if coerced-query-params
                 (assoc-in context [:request :query-params] coerced-query-params)
                 context)))}))

(defn error-interceptor [{:keys [override-humanizer]} env]
  (error-int/error-dispatch
   [ctx ex]
   [{:type :schema-tools.coerce/error}]
   (let [values    (->> ex ex-data :value)
         humanizer (or override-humanizer humanize/humanize)
         error-map (->> ex
                        ex-data
                        :error
                        (map-vals #(humanize/explain % humanizer))
                        (into {}))]
     (assoc ctx :response {:status 400 :body (json->string {:error error-map})}))

   :else
   (let [sw       (StringWriter.)
         pw       (PrintWriter. sw)
         e        (->> ex ex-data :exception)
         _        (.printStackTrace e pw)
         response (if (not= :prod env)
                    {:error       "Internal Server Error"
                     :stack-trace (str sw)}
                    {:error "Internal Server Error"})]
     (assoc ctx :response {:status 500 :body (json->string response)}))))

(defn interceptors [routes overrides env]
  [content-type
   (error-interceptor overrides env)
   (body-params)
   (body-coercer routes overrides)
   (path-params-coercer routes overrides)
   (query-params-coercer routes overrides)])

(s/defn routes->pedestal
  [routes :- schemata.http/Routes
   overrides :- (s/maybe schemata.http/Overrides)
   env :- s/Keyword
   components]
  (into
   #{}
   (map
    (fn [[route-name {:keys [path method handler]}]]
      [path
       method
       (conj (interceptors routes overrides env) (wrap-handler handler components))
       :route-name route-name]))
   routes))

(defonce ^:private server (atom nil))

(s/defrecord HttpServerImpl [routes overrides]
  component/Lifecycle
  (start [{:keys [config] :as components}]
    (let [env             (config.protocol/get-env config)
          pedestal-routes (routes->pedestal routes overrides env components)
          service         (http-server.protocol/create-server components)]
      (when (not= :test env)
        (http/start service))
      (reset! server service)
      (-> components
          (assoc :service service)
          (assoc :pedestal-routes pedestal-routes))))

  (stop [component]
    (http/stop @server)
    component)

  HttpServer
  (create-server [{:keys [config] :as components}]
    (let [env                 (config.protocol/get-env config)
          {:keys [http-port]} (config.protocol/get-config config)]
      (http/create-server
       {::http/routes          (routes->pedestal routes overrides env components)
        ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
        ::http/host            "0.0.0.0"
        ::http/type            :jetty
        ::http/port            http-port
        ::http/join?           false}))))

(s/defn new-http-server
  ([routes :- schemata.http/Routes]
   (new-http-server routes nil))
  ([routes :- schemata.http/Routes
    overrides :- (s/maybe schemata.http/Overrides)]
   (map->HttpServerImpl {:routes routes :overrides overrides})))
