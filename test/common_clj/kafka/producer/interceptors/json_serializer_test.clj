(ns common-clj.kafka.producer.interceptors.json-serializer-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.kafka.producer.interceptors.json-serializer :as nut]
            [common-clj.schema.core :as cs]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:topics
   {:my-topic {:schema {:a s/Str}}}
   :topic   :my-topic
   :message {:a "hello"}})

(deftest json-serializer
  (testing "serializes message"
    (is (= "{\"a\":\"hello\"}"
           (get-in
            (chain/execute context [nut/json-serializer])
            [:message]))))

  (testing "converts dash to underscore"
    (let [context (-> context
                      (assoc-in [:topics :my-topic :schema] {:my-age cs/PosInt})
                      (assoc-in [:message] {:my-age 25}))]
      (is (= "{\"my_age\":25}"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:message])))))

  (testing "throws error when message does not conform to schema"
    (let [context (assoc-in context [:message :b] "ciao")]
      (is (thrown-with-msg? ExceptionInfo #"Value does not match schema"
                            (chain/execute context [nut/json-serializer])))))

  (testing "throws when there's no schema specified for topic"
    (let [context (assoc-in context [:topics :my-topic :schema] nil)]
      (is (thrown-with-msg? ExceptionInfo #"Missing schema for topic :my-topic"
                            (chain/execute context [nut/json-serializer])))))

  (testing "override serialize-fn"
    (let [serialize-fn (fn [_ _ _] "schrubles")
          context      (assoc-in context
                                 [:overrides :json-serializer :serialize-fn] serialize-fn)]
      (is (= "schrubles"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:message])))))

  (testing "override extension"
    (let [extension {cs/EpochMillis (constantly "schrubles")}
          context   (-> context
                        (assoc-in [:topics :my-topic :schema] {:created-at cs/EpochMillis})
                        (assoc-in [:overrides :json-serializer :extend-serialization] extension)
                        (assoc-in [:message] {:created-at #epoch 1422554400000}))]
      (is (= "{\"created_at\":\"schrubles\"}"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:message]))))))
