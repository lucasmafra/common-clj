(ns common-clj.http-client.interceptors.deserializer-test
  (:require [cheshire.core :refer [parse-string]]
            [clojure.test :refer :all]
            [common-clj.http-client.interceptors.deserializer :as nut]
            [common-clj.json :as json]
            [io.pedestal.interceptor.chain :as chain]))

(json/json->string {:message "Hello"})

(def context
  {:request
   {:endpoint :test/hello}
   :response
   {:body "{\"message\":\"Hello\"}"}})

(deftest deserializer
  (testing "parse body as json"
    (is (= {:message "Hello"}
           (get-in
            (chain/execute context [nut/deserializer])
            [:response :body]))))

  (testing "converts underscore to dash"
    (let [context (assoc-in context [:response :body] "{\"my_message\":\"Hello\"}")]
      (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/deserializer])
                 [:response :body])))))

  (testing "converts camelcase to dash"
    (let [context (assoc-in context [:response :body] "{\"myMessage\":\"Hello\"}")]
      (is (= {:my-message "Hello"}
                (get-in
                 (chain/execute context [nut/deserializer])
                 [:response :body])))))

  (testing "override parse-key-fn"
    (let [parse-key-fn #(if (= % "message") "MESSAGE" %)
          context      (assoc-in context
                                 [:overrides :deserializer :parse-key-fn]
                                 parse-key-fn)]
      (is (= {"MESSAGE" "Hello"}
             (get-in
              (chain/execute context [nut/deserializer])
              [:response :body])))))

  (testing "override deserialize-fns"
    (let [deserialize-fns (constantly [#(parse-string % false)]) ; dont keywordize keys
          context         (assoc-in context
                                    [:overrides :deserializer :deserialize-fns]
                                    deserialize-fns)]
      (is (= {"message" "Hello"}
             (get-in
              (chain/execute context [nut/deserializer])
              [:response :body])))))

  (testing "override special-keys"
    (let [special-keys {"message" :special-key}
          context      (assoc-in context
                                 [:overrides :deserializer :special-keys]
                                 special-keys)]
      (is (= {:special-key "Hello"}
             (get-in
              (chain/execute context [nut/deserializer])
              [:response :body]))))))
