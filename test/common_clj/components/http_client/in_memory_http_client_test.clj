(ns common-clj.components.http-client.in-memory-http-client-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.http-client.in-memory-http-client :as im-hc]
            [common-clj.components.http-client.protocol :as hc-pro]
            [common-clj.generators :as gen]
            [common-clj.test-helpers :refer :all]
            [midje.sweet :refer :all]
            [schema.core :as s]))

(def endpoints
  {:a
   {:service         :a
    :path            "/a"
    :method          :get
    :response-schema s/Any}
   :b
   {:service            :b
    :path               "/b/:id"
    :path-params-schema {:id s/Uuid}
    :method             :get
    :response-schema    s/Any}})

(def id1 (gen/generate s/Uuid))
(def id2 (gen/generate s/Uuid))

#_(facts "mock-response!"
         (fact "mocked response is returned"
               (let [http-client (component/start (im-hc/new-http-client endpoints))]
                 (im-hc/mock-response! http-client :a {:body "mocked response"})
                 (hc-pro/request http-client :a) => "mocked response"))

         (fact "requesting without mocking response throws error"
               (let [http-client (component/start (im-hc/new-http-client endpoints))]
                 (hc-pro/request http-client :a) => (throws-ex {:type :http-client.error/no-response})))

         (fact "mocks forever by default - subsequent requests will get the same response"
               (let [http-client (component/start (im-hc/new-http-client endpoints))]
                 (im-hc/mock-response! http-client :a {:body "mocked response"})
                 (hc-pro/request http-client :a) => "mocked response"
                 (hc-pro/request http-client :a) => "mocked response"))

         (fact "it's possible to overwrite responses"
               (let [http-client (component/start (im-hc/new-http-client endpoints))]
                 (im-hc/mock-response! http-client :a {:body "first response"})
                 (im-hc/mock-response! http-client :a {:body "second response"})
                 (hc-pro/request http-client :a) => "second response"))

         (fact "mocking endpoint with path params"
               (let [http-client (component/start (im-hc/new-http-client endpoints))]
                 (im-hc/mock-response! http-client :b {:id id1} {:body "1"})

                 (im-hc/mock-response! http-client :b {:id id2} {:body "2"})

                 (hc-pro/request http-client :b {:id id1}) => "1"
                 (hc-pro/request http-client :b {:id id2}) => "2")))

