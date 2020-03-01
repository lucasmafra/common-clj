(ns common-clj.http-server.interceptors.query-params-coercer-test
  (:require [clojure.test :refer :all]
            [common-clj.coercion :refer [pos-int-matcher]]
            [common-clj.http-server.interceptors.query-params-coercer :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def QueryParamsSchema
  {:limit cs/PosInt})

(def context
  {:request {:query-params {:limit "10"}}
   :route   {:route-name :route/hello}
   :routes  {:route/hello
             {:query-params-schema QueryParamsSchema}}})

(deftest query-params-coercer
  (testing "coerces query-params"
    (is (= {:limit 10}
           (get-in
            (chain/execute context [nut/query-params-coercer])
            [:request :query-params]))))

  (testing "throws error when query-params does not conform to schema"
    (let [context (assoc-in context [:request :query-params :limit] "-1")]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/query-params-coercer])))))

  (testing "throws when there's query-params but no schema"
    (let [context (-> context
                      (assoc-in [:routes :route/hello :query-params-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/query-params-coercer])))))

  (testing "does not throw when there's no query-params-schema nor query-params"
    (let [context (-> context
                      (assoc-in [:request :query-params] nil)
                      (assoc-in [:routes :route/hello :query-params-schema] nil))]
      (is (= nil
             (get-in
              (chain/execute context [nut/query-params-coercer])
              [:request :query-params])))))

  (testing "override :coercers"
    (let [custom-coercers {} ; empty, doesn't know how to coerce cs/PosInt
          context         (-> context
                              (assoc-in [:overrides :query-params-coercer :coercers]
                                        custom-coercers))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/query-params-coercer])))))

  (testing "override :extension - extends coercion map"
    (let [custom-coercers {}                          ; empty, doesn't know how to coerce cs/PosInt
          extension       {cs/PosInt pos-int-matcher} ; adds cs/PosInt to coercion map
          context         (-> context
                              (assoc-in [:overrides :query-params-coercer :coercers]
                                        custom-coercers)
                              (assoc-in [:overrides :query-params-coercer :extension]
                                        extension))]
      (is (= {:limit 10}
             (get-in (chain/execute context [nut/query-params-coercer])
                     [:request :query-params]))))))
