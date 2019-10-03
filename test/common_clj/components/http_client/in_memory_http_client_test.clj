(ns common-clj.components.http-client.in-memory-http-client-test
  (:require [common-clj.components.http-client.in-memory-http-client :as im-hc]
            [common-clj.test-helpers :refer :all]
            [common-clj.components.http-client.protocol :as hc-pro]
            [midje.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [common-clj.generators :as gen]))

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

(facts "mock-response!"
  (fact "mocked response is returned"
    (let [http-client (component/start (im-hc/new-http-client endpoints))]
      (im-hc/mock-response! http-client :a {:body "mocked response"})
      (hc-pro/request http-client :a) => "mocked response"))

  (fact "requesting without mocking response throws error"
    (let [http-client (component/start (im-hc/new-http-client endpoints))]
      (hc-pro/request http-client :a) => (throws-ex {:type :http-client.error/no-response})))

  (fact "mock-response! only mocks once -> the next request will fail"
    (let [http-client (component/start (im-hc/new-http-client endpoints))]
      (im-hc/mock-response! http-client :a {:body "mocked response"})
      (hc-pro/request http-client :a) => "mocked response"
      (hc-pro/request http-client :a) => (throws-ex {:type :http-client.error/no-response})))

  (fact "mocking multiples responses when requesting the same endpoint 
         multiple times: preserves the mocking order"
    (let [http-client (component/start (im-hc/new-http-client endpoints))]
      (im-hc/mock-response! http-client :a {:body "first response"})
      (im-hc/mock-response! http-client :a {:body "second response"})
      (hc-pro/request http-client :a) => "first response"
      (hc-pro/request http-client :a) => "second response"
      (hc-pro/request http-client :a) => (throws-ex {:type :http-client.error/no-response})))

  (fact "mocking endpoint with path params"
    (let [http-client (component/start (im-hc/new-http-client endpoints))]
      (im-hc/mock-response! http-client :b {:id id1} {:body "1 - first response"})

      (im-hc/mock-response! http-client :b {:id id1} {:body "1 - second response"})

      (im-hc/mock-response! http-client :b {:id id2} {:body "2"})

      (hc-pro/request http-client :b {:id id1}) => "1 - first response"
      (hc-pro/request http-client :b {:id id2}) => "2"
      (hc-pro/request http-client :b {:id id2}) => (throws-ex {:type :http-client.error/no-response})
      (hc-pro/request http-client :b {:id id1}) => "1 - second response")))
