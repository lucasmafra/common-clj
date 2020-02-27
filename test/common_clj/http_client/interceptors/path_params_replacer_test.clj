(ns common-clj.http-client.interceptors.path-params-replacer-test
  (:require [clojure.test :refer :all]
            [common-clj.http-client.interceptors.path-params-replacer :as nut]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:endpoints
   {:service/hello
    {:path               "/api/hello/:id"
     :path-params-schema {:id s/Uuid}}}

   :endpoint :service/hello
   :options  {:path-params
              {:id #uuid "2c6c6074-3ca8-4ec3-b742-33d0fcbe0b0b"}}})

(deftest path-params-replacer
  (testing "replaces path-params and assoc to context"
    (is (= "/api/hello/2c6c6074-3ca8-4ec3-b742-33d0fcbe0b0b"
           (get-in (chain/execute context [nut/path-params-replacer])
                   [:path-replaced]))))

  (testing "when there is no dynamic path, the :path-replaced is equal to :path"
    (let [context (assoc-in context [:endpoints :service/hello :path] "/api/hello")]
      (is (= "/api/hello"
             (get-in (chain/execute context [nut/path-params-replacer])
                     [:path-replaced])))))

  (testing "throws when path-params does not conform to schema"
    (let [context (assoc-in context [:options :path-params] {:id "invalid uuid"})]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/path-params-replacer])))))

  (testing "throws when path-param value is missing"
    (let [context (assoc-in context [:options :path-params] {})]
      (is (thrown-with-msg? ExceptionInfo #"Missing path-param \"id\" on url \"/api/hello/:id\""
                   (chain/execute context [nut/path-params-replacer])))))

  (testing "throws when there are path-params but no schema"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :path-params-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/path-params-replacer])))))

  (testing "does not throw when there's no path-params-schema nor path-params"
    (let [context (-> context
                      (assoc-in [:options :path-params] nil)
                      (assoc-in [:endpoints :service/hello :path-params-schema] nil)
                      (assoc-in [:endpoints :service/hello :path] "/api/hello"))]
      (is (= "/api/hello"
             (get-in (chain/execute context [nut/path-params-replacer])
                     [:path-replaced]))))))
