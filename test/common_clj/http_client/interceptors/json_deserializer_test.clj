(ns common-clj.http-client.interceptors.json-deserializer-test
  (:require [cheshire.core :refer [parse-string]]
            [clojure.test :refer :all]
            [common-clj.http-client.interceptors.json-deserializer :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(def context
  {:endpoint :service/test
   :response
   {:body "{\"message\":\"Hello\"}"}})

(deftest json-deserializer
  (testing "parse body as json"
    (is (= {:message "Hello"}
           (get-in
            (chain/execute context [nut/json-deserializer])
            [:response :body]))))

  (testing "converts underscore to dash"
    (let [context (assoc-in context [:response :body] "{\"my_message\":\"Hello\"}")]
      (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/json-deserializer])
                 [:response :body])))))

  (testing "converts camelcase to dash"
    (let [context (assoc-in context [:response :body] "{\"myMessage\":\"Hello\"}")]
      (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/json-deserializer])
                 [:response :body])))))

  (testing "override deserialize-fn"
    (let [deserialize-fn #(parse-string % false) ; dont keywordize keys
          context         (assoc-in context
                                    [:overrides :json-deserializer :deserialize-fn]
                                    deserialize-fn)]
      (is (= {"message" "Hello"}
             (get-in
              (chain/execute context [nut/json-deserializer])
              [:response :body])))))

  (testing "override extension"
    (let [special-keys {"message" :special-key}
          context      (assoc-in context
                                 [:overrides :json-deserializer :extension]
                                 special-keys)]
      (is (= {:special-key "Hello"}
             (get-in
              (chain/execute context [nut/json-deserializer])
              [:response :body]))))))
