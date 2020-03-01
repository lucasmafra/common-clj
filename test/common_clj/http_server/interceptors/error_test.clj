(ns common-clj.http-server.interceptors.error-test
  (:require [clojure.test :refer [deftest is testing]]
            [common-clj.coercion :as coercion]
            [common-clj.http-server.interceptors.error :as nut]
            [common-clj.json :as json]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(defn- make-validation-error [schema value]
  (try
    (coercion/coerce schema value coercion/default-coercion-map)
    (catch ExceptionInfo e
      (-> e ex-data :error))))

(def context
  {::chain/error
   (ex-info "Deu ruim"
            {:execution-id 1
             :stage        :enter
             :interceptor  ::some-interceptor
             :type         :schema-tools.coerce/error
             :exception    (Exception. "Deu ruim mesmo")
             :error        (make-validation-error {:a s/Int} {:a "bla" :b 1})})})

(deftest coerce-error
  (testing "returns 400 (bad request) when exception is of type coerce"
    (is (= 400
           (get-in
            (chain/execute context [nut/error])
            [:response :status]))))

  (testing "body contains helpful description of the error"
    (is (= (json/json->string
            {:error {:a "[not [integer? \"bla\"]]"
                     :b "disallowed-key"}})
           (get-in
            (chain/execute context [nut/error])
            [:response :body])))))

(deftest unknown-error
  (let [context (assoc-in context [::chain/error] (ex-info "Unknown" {:reason :unknown}))]
    (with-redefs [println (constantly nil)] ; otherwise it would polute repl
      (testing "returns 500 (internal server error) when does not know how to handle"
        (is (= 500
               (get-in
                (chain/execute context [nut/error])
                [:response :status])))))))
