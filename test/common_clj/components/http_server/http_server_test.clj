(ns common-clj.components.http-server.http-server-test
  (:require [common-clj.components.http-server.http-server :as http-server]
            [com.stuartsierra.component :as component]
            [common-clj.schemata.http :as schemata.http]
            [selvage.midje.flow :refer [*world* flow]]
            [common-clj.test-helpers :refer [init!]]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [io.pedestal.test :as test]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [io.pedestal.http :as http]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [io.pedestal.http.route :as http.routes]
            [matcher-combinators.midje :refer [match]]
            [common-clj.json :refer [json->string]]))

(defn handler-a [{:keys [body json-params] :as request} {:keys [counter-a logger]}]
  (println json-params)
  (counter.protocol/inc! counter-a)
  (logger.protocol/log! logger :req-body body)
  {:status 200})

(defn handler-b [request {:keys [counter-b]}]
  (counter.protocol/inc! counter-b)
  {:status 200})

(def SchemaA
  {:name s/Str
   :age s/Int
   :budget java.math.BigDecimal
   :admin s/Bool
   :city s/Keyword
   :date java.time.LocalDate
   :date-time java.time.LocalDateTime
   :control-key s/Uuid})

(s/def valid-request-body :- SchemaA
  {:name        "John"
   :age         22
   :budget      200M
   :admin       true
   :city        :sp
   :date        #date "2019-08-22"
   :date-time   #date-time "2019-08-22T09:23:17"
   :control-key #uuid "b348f264-b9de-429a-9550-69499af20874"})

(s/def routes :- schemata.http/Routes
  {:a
   {:path            "/a" 
    :method          :post
    :handler         handler-a
    :request-schema  SchemaA
    :response-schema s/Any}

   :b
   {:path            "/b/:id"
    :method          :get
    :handler         handler-b
    :response-schema s/Any}})

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
        => [valid-request-body]))))
