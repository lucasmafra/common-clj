(ns common-clj.http-client.interceptors.url-builder-test
  (:require [clojure.test :refer :all]
            [common-clj.http-client.interceptors.url-builder :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(def context
  {:endpoints
   {:service/hello
    {:host "http://service.com"}}

   :request
   {:endpoint      :service/hello
    :path-replaced "/api/hello"}})

(deftest url-builder
  (testing "builds url and assocs to context"
    (is (= "http://service.com/api/hello"
           (get-in (chain/execute context [nut/url-builder])
                 [:request :url]))))

  (testing "when host is a variable, gets the value from config"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :host] "{{my-service}}")
                      (assoc-in [:config :known-hosts :my-service] "http://my-service.com"))]
      (is (= "http://my-service.com/api/hello"
           (get-in (chain/execute context [nut/url-builder])
                   [:request :url]))))))
