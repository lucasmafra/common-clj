(ns common-clj.http-client.interceptors.request-body-test
  (:require [clojure.test :refer :all]
            [common-clj.http-client.interceptors.request-body :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:endpoints
   {:service/hello
    {:request-schema {:age cs/PosInt}}}

   :request
   {:endpoint :service/hello
    :options  {:body {:age 25}}}})

(deftest query-params
  (testing "returns context when req body conforms to schema"
    (is (= context
           (chain/execute context [nut/request-body]))))

  (testing "throws error when req body does not conform to schema"
    (let [context (assoc-in context [:request :options :body :age] -25)]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/request-body])))))

  (testing "throws when there's request body but no schema"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :request-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/request-body])))))

  (testing "does not throw when there's no request schema nor body"
    (let [context (-> context
                      (assoc-in [:request :options :body] nil)
                      (assoc-in [:endpoints :service/hello :request-schema] nil))]
      (is (= context
             (chain/execute context [nut/request-body]))))))
