(ns common-clj.components.http-server.in-memory-http-server-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.http-server.in-memory-http-server
             :as in-memory-http-server]
            [common-clj.schemata.http :as schemata.http]
            [midje.sweet :refer :all]
            [schema.core :as s]))

(defn echo [request]
  {:status 200 :body request})

(s/def routes :- schemata.http/Routes
  {:create-order
   {:path            "/orders" 
    :method          :post
    :handler         echo
    :request-schema  s/Any
    :response-schema s/Any}

   :get-order
   {:path            "/orders/:id"
    :method          :get
    :handler         echo
    :response-schema s/Any}})

(def pedestal-routes
  #{["/orders" :post echo :route-name :create-order]
    ["/orders/:id" :get echo :route-name :get-order]})

(s/with-fn-validation
  (fact "routes->pedestal"
    (in-memory-http-server/routes->pedestal routes)
    => pedestal-routes))
