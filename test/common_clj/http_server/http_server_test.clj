(ns common-clj.http-server.http-server-test
  (:require [aux.http-server :refer [GET<no-print>]]
            [aux.init :refer [defflow init!]]
            [com.stuartsierra.component :as component]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-server.http-server :as nut]
            [common-clj.http-server.interceptors.helpers :refer [ok]]
            [common-clj.state-flow-helpers.http-server :refer [GET POST]]
            [schema.core :as s]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def config
  {:app-name :my-app})

(def routes
  {:route/simple-get
   {:path            "/simple-get"
    :method          :get
    :response-schema {:message s/Str}
    :handler         (constantly (ok {:message "Hello"}))}

   :route/simple-post
   {:path            "/simple-post"
    :method          :post
    :request-schema  {:name s/Str :age s/Int}
    :response-schema {:name s/Str :age s/Int}
    :handler         (fn [{{:keys [name age]} :body} _]
                       (ok {:name name :age age}))}

   :route/response-does-not-conform-to-schema
   {:path            "/response-does-not-conform-to-schema"
    :method          :get
    :response-schema {:message s/Str}
    :handler         (constantly (ok {:name "John Doe"}))}})

(def system
  (component/system-map
   :config (imc/new-config config :test)

   :http-server (component/using
                 (nut/new-http-server routes)
                 [:config])))

(defflow simple-get
  :pre-conditions [(init! system)]

  [response (GET "/simple-get")]

  (match? {:status 200 :body {"message" "Hello"}}
          response))

(defflow simple-post
  :pre-conditions [(init! system)]

  [response (POST "/simple-post"
              :body {"name" "John Doe" "age" 25}
              :headers {"Content-Type" "application/json"})]

  (match? {:status 200 :body {"name" "John Doe" "age" 25}}
          response))

(defflow throw-400-when-request-does-not-conform-to-schema
  :pre-conditions [(init! system)]

  [response (POST "/simple-post")]

  (match? {:status 400}
          response))

(defflow throw-500-when-response-does-not-conform-to-schema
  :pre-conditions [(init! system)]

  [response (GET<no-print> "/response-does-not-conform-to-schema")]

  (match? {:status 500}
          response))
