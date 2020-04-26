(ns common-clj.kafka.consumer.interceptors.json-deserializer-test
  (:require [cheshire.core :refer [parse-string]]
            [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.consumer.interceptors.json-deserializer :as nut]
            [io.pedestal.interceptor.chain :as chain])
  (:import org.apache.kafka.clients.consumer.ConsumerRecord))

(defn context [message]
  {:record (new ConsumerRecord "TOPIC" 0 0 0 message)})

(deftest json-deserializer
  (testing "parses kafka record as json"
    (is (= {:a "hello"}
           (get-in
            (chain/execute (context #json {"a" "hello"}) [nut/json-deserializer])
            [:message]))))

  (testing "converts underscore to dash"
    (is (= {:my-message "hello"}
           (get-in
            (chain/execute (context #json {"my_message" "hello"}) [nut/json-deserializer])
            [:message]))))

  (testing "converts camelcase to dash"
    (is (= {:my-message "hello"}
           (get-in
            (chain/execute (context #json {"myMessage" "hello"}) [nut/json-deserializer])
            [:message]))))

  (testing "override deserialize-fn"
    (let [deserialize-fn #(parse-string % false) ; dont keywordize keys
          ctx            (assoc-in (context #json {"a" "hello"})
                                   [:overrides :json-deserializer :deserialize-fn]
                                   deserialize-fn)]
      (is (= {"a" "hello"}
             (get-in
              (chain/execute ctx [nut/json-deserializer])
              [:message])))))

  (testing "override extension"
    (let [special-keys {"a" :bla}
          ctx          (assoc-in (context #json {"a" "hello"})
                                 [:overrides :json-deserializer :extend-deserialization]
                                 special-keys)]
      (is (= {:bla "hello"}
             (get-in
              (chain/execute ctx [nut/json-deserializer])
              [:message]))))))
