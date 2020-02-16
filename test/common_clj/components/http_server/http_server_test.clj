(ns common-clj.components.http-server.http-server-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [common-clj.components.http-server.http-server :as http-server]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.json :refer [string->json]]
            [common-clj.schemata.http-server :as schemata.http]
            [common-clj.test-helpers :refer [init! request-arrived!]]
            [matcher-combinators.midje :refer [match]]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [selvage.midje.flow :refer [*world* flow]]
            [common-clj.generators :as gen]))

(def id (gen/generate s/Uuid))
(def another-id (gen/generate s/Uuid))

(defn handler-a [{:keys [body path-params] :as request} {:keys [counter-a logger]}]
  (counter.protocol/inc! counter-a)
  (logger.protocol/log! logger :req-body body)
  (logger.protocol/log! logger :id (:id path-params))
  {:status 200
   :body {:message "Hello"}})

(defn handler-b [request {:keys [counter-b]}]
  (counter.protocol/inc! counter-b)
  (http-server/ok {:id id}))

(def RequestA
  {(s/optional-key :name) s/Str
   :date                  java.time.LocalDate})

(def valid-request-body (gen/generate RequestA))
(def another-valid-request-body (gen/generate RequestA))

(def invalid-request-body
  {:name "John"})

(def ResponseB
  {:name s/Str
   :id   s/Uuid})

(s/def routes :- schemata.http/Routes
  {:a
   {:path               "/a/:id" 
    :method             :post
    :handler            handler-a
    :path-params-schema {:id s/Uuid}
    :request-schema     RequestA
    :response-schema    s/Any}

   :b
   {:path               "/b/:id"
    :method             :get
    :handler            handler-b
    :path-params-schema {:id s/Uuid}
    :response-schema    ResponseB}})

(def app-config
  {:app-name :common-clj
   :http-port 8080})

(def system
  (component/system-map
   :config      (in-memory-config/new-config app-config :test)
   :counter-a   (in-memory-counter/new-counter)
   :counter-b   (in-memory-counter/new-counter)
   :logger      (in-memory-logger/new-logger)
   :http-server (component/using
                 (http-server/new-http-server routes)
                 [:config :counter-a :counter-b :logger])))

#_(s/with-fn-validation
  (flow "init server"
        (partial init! system)

        (fact "routes->pedestal"
          (-> *world* :system :http-server :pedestal-routes)
          => (match #{["/a/:id" :post irrelevant :route-name :a]
                      ["/b/:id" :get irrelevant :route-name :b]})))

  (flow "valid request arrives"
        (partial init! system)

        (partial request-arrived! :a {:body        valid-request-body
                                      :path-params {:id id}})

        (partial request-arrived! :a {:body        another-valid-request-body
                                      :path-params {:id another-id}})

        (fact "corresponding handler is called"
          (-> *world* :system :counter-a counter.protocol/get-count) => 2)

        (fact "other handlers are not called"
          (-> *world* :system :counter-b counter.protocol/get-count) => 0)

        (fact "request body is coerced"
          (-> *world* :system :logger (logger.protocol/get-logs :req-body))
          => [valid-request-body another-valid-request-body])

        (fact "path-params are coerced"
          (-> *world* :system :logger (logger.protocol/get-logs :id))
          => [id another-id])

        (fact "status 200 is returned"
          (-> *world* :http-responses :a first :status)
          => 200)

        (fact "content type is application/json"
          (-> *world* :http-responses :a first :headers)
          => (match {"Content-Type" "application/json"}))

        (fact "response body is valid json"
          (-> *world* :http-responses :a first :body)
          => {:message "Hello"}))

  (flow "invalid request arrives"
        (partial init! system)

        (partial request-arrived! :a {:body           invalid-request-body
                                      :path-params    {:id id}
                                      :supress-errors true})

        (fact "handler is not executed"
          (-> *world* :system :counter-a counter.protocol/get-count) => 0)

        (fact "status 400 is returned"
          (-> *world* :http-responses :a first :status) => 400))

  (flow "invalid response body"
        (partial init! system)

        (partial request-arrived! :b {:path-params    {:id id}
                                      :supress-errors true})

        (fact "status 500 is returned"
          (-> *world* :http-responses :b first :status) => 500))

  (flow "invalid path-param"
        (partial init! system)

        (partial request-arrived! :b {:path-params    {:id "invalid-uuid"}
                                      :supress-errors true})

        (fact "400 is returned"
          (-> *world* :http-responses :b first :status) => 400))
  
  (future-fact "starts server on port passed via config"))
