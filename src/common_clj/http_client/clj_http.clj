(ns common-clj.http-client.clj-http
  (:require [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [common-clj.coercion :as coercion]
            [common-clj.components.config.protocol :as conf-pro]
            [common-clj.http-client.interceptors.context-initializer :as i-ctx]
            [common-clj.http-client.interceptors.handler :as i-handler]
            [common-clj.http-client.interceptors.path-params-replacer :as i-path]
            [common-clj.http-client.interceptors.query-params :as i-query]
            [common-clj.http-client.interceptors.request-body :as i-req]
            [common-clj.http-client.interceptors.response-body-coercer
             :as
             i-resp-coercer]
            [common-clj.http-client.interceptors.response-body-parser
             :as
             i-resp-parser]
            [common-clj.http-client.interceptors.url-builder :as i-url]
            [common-clj.http-client.protocol :as hc-pro]
            [common-clj.schemata.http-client :as s-hc]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s]))

(defn default-interceptors [m]
  [; enter interceptors
   (i-ctx/context-initializer m)
   i-path/path-params-replacer
   i-query/query-params
   i-req/request-body
   i-url/url-builder
   i-handler/handler

   ; leave interceptors
   i-resp-coercer/response-body-coercer
   i-resp-parser/response-body-parser])

(s/defrecord CljHttp [endpoints :- s-hc/Endpoints]
  component/Lifecycle
  (start [component]
    (s/validate s-hc/Endpoints endpoints)
    component)

  (stop [component]
    component)

  hc-pro/HttpClient
  (request [component endpoint]
    (hc-pro/request component endpoint {}))

  (request [{:keys [config]} endpoint options]
    (let [config (conf-pro/get-config config)
          interceptors (default-interceptors {:endpoints endpoints
                                              :endpoint  endpoint
                                              :options   options
                                              :coercers  coercion/default-coercers
                                              :config    config})]
      (:response
       (chain/execute {} interceptors)))))

(s/defn new-http-client [endpoints :- s-hc/Endpoints]
  (map->CljHttp {:endpoints endpoints}))
