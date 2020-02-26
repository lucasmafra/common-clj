(ns common-clj.http-client.interceptors.query-params-test
  (:require [clojure.test :refer :all]
            [common-clj.http-client.interceptors.query-params :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:endpoints
   {:service/hello
    {:query-params-schema {:age cs/PosInt}}}

   :request
   {:endpoint :service/hello
    :options  {:query-params {:age 25}}}})

(deftest query-params
  (testing "returns context when query-params conforms to schema"
    (is (= context
           (chain/execute context [nut/query-params]))))

  (testing "throws error when query-params does not conform to schema"
    (let [context (assoc-in context [:request :options :query-params :age] -25)]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/query-params])))))

  (testing "throws when there are query-params but no schema"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :query-params-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/query-params])))))

  (testing "does not throw when there's no query-params-schema nor query-params"
    (let [context (-> context
                      (assoc-in [:request :options :query-params] nil)
                      (assoc-in [:endpoints :service/hello :query-params-schema] nil))]
      (is (= context
             (chain/execute context [nut/query-params]))))))
