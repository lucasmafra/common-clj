(ns common-clj.http-client.interceptors.response-body-parser-test
  (:require  [clojure.test :refer :all]
             [common-clj.http-client.interceptors.response-body-parser :as nut]
             [common-clj.json :as json]
             [io.pedestal.interceptor.chain :as chain]))

(json/json->string {:message "Hello"})

(def context
  {:response
   {:body "{\"message\":\"Hello\"}"}})

(deftest response-body-parser
  (testing "parse body as json"
    (is (= {:message "Hello"}
           (get-in
            (chain/execute context [nut/response-body-parser])
            [:response :body]))))

  (testing "converts underscore to dash"
    (let [context (assoc-in context [:response :body] "{\"my_message\":\"Hello\"}")]
         (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/response-body-parser])
                 [:response :body])))))

  (testing "converts camelcase to dash"
    (let [context (assoc-in context [:response :body] "{\"myMessage\":\"Hello\"}")]
         (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/response-body-parser])
                 [:response :body]))))))
