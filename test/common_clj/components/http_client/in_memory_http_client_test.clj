(ns common-clj.components.http-client.in-memory-http-client-test
  (:require [common-clj.components.http-client.in-memory-http-client :as im-hc]
            [common-clj.test-helpers :refer :all]
            [common-clj.components.http-client.protocol :as hc-pro]
            [midje.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(def endpoints
  {:a
   {:service         :a
    :path            "/a"
    :method          :get
    :response-schema s/Any}})

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
      (hc-pro/request http-client :a) => (throws-ex {:type :http-client.error/no-response}))))
