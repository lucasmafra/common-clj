(ns common-clj.lib.pedestal-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.lib.pedestal :as pedestal]
            [common-clj.schemata.http :as schemata.http]
            [midje.sweet :refer :all]
            [schema.core :as s]
            [io.pedestal.interceptor.chain :as chain]))

(def CreateOrderSchema
  {:id          s/Uuid
   :date        java.time.LocalDate
   :total       java.math.BigDecimal
   :description s/Str})

(s/def routes :- schemata.http/Routes
  {:create-order
   {:path            "/orders" 
    :method          :post
    :handler         identity
    :request-schema  CreateOrderSchema
    :response-schema s/Any}

   :get-order
   {:path            "/orders/:id"
    :method          :get
    :handler         identity
    :response-schema s/Any}})

(def pedestal-routes
  #{["/orders" :post identity :route-name :create-order]
    ["/orders/:id" :get identity :route-name :get-order]})

(def valid-body
  {:id          "01be2d6c-4f0b-425b-a689-d25f85100b6a"
   :date        "2019-09-18"
   :total       200
   :description "Fake order"})

(s/def coerced-body :- CreateOrderSchema
  {:id          #uuid "01be2d6c-4f0b-425b-a689-d25f85100b6a"
   :date        #date "2019-09-18"
   :total       200M
   :description "Fake order"})

(s/with-fn-validation
  (fact "routes->pedestal"
    (pedestal/routes->pedestal routes)
    => pedestal-routes)

  (fact "coerce-request"
    (-> {:json-params valid-body}
        (chain/enqueue [(pedestal/coerce-request CreateOrderSchema)])
        chain/execute)
    => (match {:body coerced-body})))
