(ns common-clj.http-server.interceptors.path-params-coercer-test
  (:require [clojure.test :refer :all]
            [common-clj.coercion :refer [pos-int-matcher]]
            [common-clj.http-server.interceptors.path-params-coercer :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:request {:path-params {:id "25"}}
   :route   {:route-name :route/hello}
   :routes  {:route/hello
             {:path-params-schema {:id cs/PosInt}}}})

(deftest path-params-coercer
  (testing "coerces path-params"
    (is (= {:id 25}
           (get-in
            (chain/execute context [nut/path-params-coercer])
            [:request :path-params]))))

  (testing "throws error when path-params does not conform to schema"
    (let [context (assoc-in context [:request :path-params] "not an integer")]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/path-params-coercer])))))

  (testing "throws when there's path-params but no schema"
    (let [context (-> context
                      (assoc-in [:routes :route/hello :path-params-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/path-params-coercer])))))

  (testing "does not throw when there's no path-params-schema nor query-params"
    (let [context (-> context
                      (assoc-in [:request :path-params] nil)
                      (assoc-in [:routes :route/hello :path-params-schema] nil))]
      (is (= nil
             (get-in
              (chain/execute context [nut/path-params-coercer])
              [:request :path-params])))))

  (testing "override :coercers"
    (let [custom-coercers {} ; empty, doesn't know how to coerce cs/PosInt
          context         (-> context
                              (assoc-in [:overrides :path-params-coercer :coercers]
                                        custom-coercers))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/path-params-coercer])))))

  (testing "override :extension - extends coercion map"
    (let [custom-coercers {}                          ; empty, doesn't know how to coerce cs/PosInt
          extension       {cs/PosInt pos-int-matcher} ; adds cs/PosInt to coercion map
          context         (-> context
                              (assoc-in [:overrides :path-params-coercer :coercers]
                                        custom-coercers)
                              (assoc-in [:overrides :path-params-coercer :extension]
                                        extension))]
      (is (= {:id 25}
             (get-in (chain/execute context [nut/path-params-coercer])
                     [:request :path-params]))))))
