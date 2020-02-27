(ns common-clj.http-client.clj-http
  (:require [com.stuartsierra.component :as component]
            [common-clj.coercion :as coercion]
            [common-clj.components.config.protocol :as conf-pro]
            [common-clj.http-client.interceptors.coercer :as i-coercer]
            [common-clj.http-client.interceptors.context-initializer :as i-ctx]
            [common-clj.http-client.interceptors.deserializer :as i-deserializer]
            [common-clj.http-client.interceptors.handler :as i-handler]
            [common-clj.http-client.interceptors.path-params-replacer :as i-path]
            [common-clj.http-client.interceptors.query-params :as i-query]
            [common-clj.http-client.interceptors.request-body :as i-req]
            [common-clj.http-client.interceptors.url-builder :as i-url]
            [common-clj.http-client.interceptors.with-mock-calls :as i-mock]
            [common-clj.http-client.protocol :as hc-pro]
            [common-clj.schemata.http-client :as s-hc]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s]))

(def default-interceptors
  [; enter interceptors
   i-path/path-params-replacer
   i-query/query-params
   i-req/request-body
   i-url/url-builder
   i-handler/handler

   ; leave interceptors
   i-coercer/coercer
   i-deserializer/deserializer])

(defn build-interceptors [{:keys [env] :as m}]
  (cond->> default-interceptors
    (= :test env) (cons i-mock/with-mock-calls)
    
    true (cons (i-ctx/context-initializer m))))

(s/defrecord CljHttp [endpoints :- s-hc/Endpoints
                      overrides :- s-hc/Overrides]
  component/Lifecycle
  (start [component]
    (s/validate s-hc/Endpoints endpoints)
    component)

  (stop [component]
    component)

  hc-pro/HttpClient
  (request [component endpoint]
    (hc-pro/request component endpoint {}))

  (request [{:keys [config] :as component} endpoint options]
    (let [{:keys [known-hosts]} (conf-pro/get-config config)
          env                   (conf-pro/get-env config)
          interceptors          (build-interceptors {:endpoints   endpoints
                                                     :endpoint    endpoint
                                                     :options     options
                                                     :known-hosts known-hosts
                                                     :env         env
                                                     :overrides   overrides})]
      (:response
       (chain/execute {} interceptors)))))

(s/defn new-http-client
  ([endpoints :- s-hc/Endpoints]
   (new-http-client endpoints {}))
  ([endpoints :- s-hc/Endpoints
    overrides :- s-hc/Overrides]
   (map->CljHttp {:endpoints endpoints
                  :overrides overrides})))
