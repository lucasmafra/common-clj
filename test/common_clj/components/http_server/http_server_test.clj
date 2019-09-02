(ns common-clj.components.http-server.http-server-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [common-clj.components.http-server.http-server :as http-server]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.json :refer [json->string string->json]]
            [common-clj.schemata.http :as schemata.http]
            [common-clj.test-helpers :refer [init!]]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as http.routes]
            [io.pedestal.test :as test]
            [matcher-combinators.midje :refer [match]]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [selvage.midje.flow :refer [*world* flow]]))

(def id #uuid "37929dae-f41d-40d5-b512-729b623d6ea7")

(defn handler-a [{:keys [body] :as request} {:keys [counter-a logger]}]
  (counter.protocol/inc! counter-a)
  (logger.protocol/log! logger :req-body body)
  {:status 200 :body {:message "Hello"}})

(defn handler-b [request {:keys [counter-b]}]
  (counter.protocol/inc! counter-b)
  {:status 200 :body {:id id}})

(def RequestA
  {:name        s/Str
   :date        java.time.LocalDate})

(s/def valid-request-body :- RequestA
  {:name        "John"
   :date        #date "2019-08-22"})

(def invalid-request-body
 {:name "John"})

(def ResponseB
  {:name s/Str
   :id s/Uuid})

(s/def routes :- schemata.http/Routes
  {:a
   {:path            "/a" 
    :method          :post
    :handler         handler-a
    :request-schema  RequestA
    :response-schema s/Any}

   :b
   {:path            "/b/:id"
    :method          :get
    :handler         handler-b
    :response-schema ResponseB}})

(def app-config
  {:app-name :common-clj
   :http-port 8080})

(def system
  (component/system-map
   :config      (in-memory-config/new-config app-config)
   :counter-a   (in-memory-counter/new-counter)
   :counter-b   (in-memory-counter/new-counter)
   :logger      (in-memory-logger/new-logger)
   :http-server (component/using
                 (http-server/new-http-server routes)
                 [:config :counter-a :counter-b :logger])))

(defn http-request!
  ([route world]
   (http-request! route {} world))
  
  ([route {:keys [path-params body]} world]
   (let [service (-> world :system :http-server :service :io.pedestal.http/service-fn)
         routes (-> world :system :http-server :routes)
         pedestal-routes (-> world :system :http-server :pedestal-routes)
         url-for (http.routes/url-for-routes
                  (http.routes/expand-routes pedestal-routes))
         {:keys [method path]} (route routes)
         response (test/response-for
                   service method
                   (url-for route :path-params path-params)
                   :headers {"Content-Type" "application/json"}
                   :body (json->string body))]
     (update-in world [:http-responses route] conj response))))

(with-redefs [http/start identity]
  (s/with-fn-validation
    (flow "init server"
      (partial init! system)

      (fact "routes->pedestal"
        (-> *world* :system :http-server :pedestal-routes)
        => (match #{["/a" :post irrelevant :route-name :a]
                    ["/b/:id" :get irrelevant :route-name :b]})))

    (flow "valid request arrives"
      (partial init! system)

      (partial http-request! :a {:body valid-request-body})

      (fact "corresponding handler is called"
        (-> *world* :system :counter-a counter.protocol/get-count) => 1)

      (fact "other handlers are not called"
        (-> *world* :system :counter-b counter.protocol/get-count) => 0)

      (fact "request body is coerced"
        (-> *world* :system :logger (logger.protocol/get-logs :req-body))
        => [valid-request-body])

      (fact "status 200 is returned"
        (-> *world* :http-responses :a first :status)
        => 200)

      (fact "content type is application/json"
        (-> *world* :http-responses :a first :headers)
        => (match {"Content-Type" "application/json"}))

      (fact "response body is valid json"
        (-> *world* :http-responses :a first :body string->json)
        => {:message "Hello"}))

    (flow "invalid request arrives"
      (partial init! system)

      (partial http-request! :a {:body invalid-request-body})

      (fact "handler is not executed"
        (-> *world* :system :counter-a counter.protocol/get-count) => 0)

      (fact "status 400 is returned"
        (-> *world* :http-responses :a first :status) => 400))

    (flow "invalid response body"
      (partial init! system)

      (partial http-request! :b {:path-params {:id (str id)}})

      (fact "status 500 is returned"
        (-> *world* :http-responses :b first :status) => 500))

    (future-fact "starts server on port passed via config")))
