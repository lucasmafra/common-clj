(ns common-clj.http-server.default-interceptors
  (:require [common-clj.http-server.interceptors.body-coercer :as i-body-coercer]
            [common-clj.http-server.interceptors.content-type :as i-content-type]
            [common-clj.http-server.interceptors.error :as i-error]
            [common-clj.http-server.interceptors.json-serializer :as i-json-serializer]
            [common-clj.http-server.interceptors.path-params-coercer :as i-path-params-coercer]
            [common-clj.http-server.interceptors.query-params-coercer :as i-query-params-coercer]
            [io.pedestal.http.body-params :refer [body-params]]))

(def default-handler-interceptors
  [i-error/error
   (body-params)
   i-body-coercer/body-coercer
   i-path-params-coercer/path-params-coercer
   i-query-params-coercer/query-params-coercer
   i-json-serializer/json-serializer
   i-content-type/content-type])
