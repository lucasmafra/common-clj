(ns common-clj.http-client.http-client-test
  (:require [aux.init :refer [defflow init!]]
            [com.stuartsierra.component :as component]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-client.http-client :as nut]
            [common-clj.http-client.interceptors.with-mock-calls :as i-mock]
            [common-clj.http-client.protocol :as hc-pro]
            [common-clj.schema.core :as cs]
            [common-clj.state-flow-helpers.http-client :as http-client]
            [schema.core :as s]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.state :as state]))

(def ResponseSchema
  {:message s/Str})

(def endpoints
  {:test/simple-get
   {:host            "http://test.com"
    :method          :get
    :path            "/test/get"
    :response-schema s/Any}

   :test/more-features
   {:host               "{{my-service}}"
    :method             :post
    :path               "/test/more-features/:id"
    :request-schema     {:age cs/PosInt}
    :path-params-schema {:id s/Uuid}
    :response-schema    {:my-name s/Str
                         :date cs/LocalDate}}})

(def config
  {:app/name :my-app})

(def system
  (component/system-map
   :config (imc/new-config config :test)
   :http-client (component/using
                 (nut/new-http-client endpoints)
                 [:config])))

(def mock-calls
  {"http://test.com/test/get"
   (constantly {:status 200 :body {"message" "Hello"}})

   "{{my-service}}/test/more-features/2c6c6074-3ca8-4ec3-b742-33d0fcbe0b0b"
   {:status 200 :body {"my_name" "Tester"
                       "date"    "2019-08-02"}}})

(defn request
  ([endpoint] (request endpoint {}))
  ([endpoint options] (state/gets (fn [{{:keys [http-client]} :system}]
                                    (hc-pro/request http-client endpoint options)))))

(defflow simple-get-request
  :pre-conditions [(init! system)
                   (http-client/mock! mock-calls)]

  [response (request :test/simple-get)]
  (match? {:status 200 :body {:message "Hello"}}
          response))

(defflow request-with-path-params
  :pre-conditions [(init! system)
                   (http-client/mock! mock-calls)]

  [response (request :test/more-features
                     {:path-params {:id #uuid "2c6c6074-3ca8-4ec3-b742-33d0fcbe0b0b"}
                      :body        {:age 25}})]

  (match? {:status 200 :body {:my-name "Tester"
                              :date    #local-date "2019-08-02"}}
          response))

(defflow test-extend-deserialization
  :pre-conditions [(init! system)
                   (http-client/mock! mock-calls)]

  [response (request :test/simple-get {:overrides
                                       {:extend-deserialization
                                        {"message" :special-key}}})]

  (match? {:status 200 :body {:special-key "Hello"}}
          response))

(defflow test-extra-interceptors
  :pre-conditions
  [(init! (component/system-map
           :config (imc/new-config config :prod)
           :http-client (component/using
                         (nut/new-http-client
                          endpoints {:extra-interceptors [(i-mock/with-mock-calls mock-calls)]})
                         [:config])))]

  [response (request :test/simple-get)]

  (match? {:status 200 :body {:message "Hello"}}
          response))
