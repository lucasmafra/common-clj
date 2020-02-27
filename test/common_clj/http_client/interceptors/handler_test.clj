(ns common-clj.http-client.interceptors.handler-test
  (:require [clj-http.fake :refer [with-fake-routes]]
            [clojure.test :refer :all]
            [common-clj.http-client.interceptors.handler :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(def context
  {:endpoints
   {:service/hello
    {:method :get}}

   :endpoint :service/hello
   :url      "http://service.com/api/hello"})

(def mock-calls
  {"http://service.com/api/hello"     {:get (constantly {:status 200 :body "Hello"})}
   "http://service.com/api/hello?x=1" {:get (constantly {:status 200 :body "x = 1"})}})

(deftest handler
  (testing "calls clj-http library and assoc response to context"
    (is (= "Hello"
           (get-in (with-fake-routes mock-calls
                     (chain/execute context [nut/handler]))
                 [:response :body]))))

  (testing "passes options to clj-http library"
    (let [context (assoc context :options {:query-params {:x 1}})]
      (is (= "x = 1"
           (get-in (with-fake-routes mock-calls
                     (chain/execute context [nut/handler]))
                 [:response :body]))))))
