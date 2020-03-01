(ns common-clj.http-client.interceptors.json-serializer-test
  (:require [clojure.test :refer :all]
            [common-clj.http-client.interceptors.json-serializer :as nut]
            [common-clj.json :as json]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:endpoints
   {:service/hello {:request-schema {:age cs/PosInt}}
    :service/bye   {:request-schema {:created-at cs/EpochMillis}}}
   :endpoint :service/hello
   :options  {:body {:age 25}}})

(deftest json-serializer
  (testing "serializes req body"
    (is (= "{\"age\":25}"
           (get-in
            (chain/execute context [nut/json-serializer])
            [:options :body]))))

  (testing "converts dash to underscore"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :request-schema] {:my-age cs/PosInt})
                      (assoc-in [:options :body] {:my-age 25}))]

      (is (= "{\"my_age\":25}"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:options :body])))))

  (testing "throws error when req body does not conform to schema"
    (let [context (assoc-in context [:options :body :age] -25)]
      (is (thrown-with-msg? ExceptionInfo #"Value does not match schema"
                            (chain/execute context [nut/json-serializer])))))

  (testing "throws when there's request body but no schema"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :request-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/json-serializer])))))

  (testing "does nothing when there's no request schema nor body"
    (let [context (-> context
                      (assoc-in [:options :body] nil)
                      (assoc-in [:endpoints :service/hello :request-schema] nil))]
      (is (= context
             (chain/execute context [nut/json-serializer])))))

  (testing "override serialize-fn"
    (let [serialize-fn (fn [_ _ _] "schrubles")
          context (assoc-in context [:overrides :json-serializer :serialize-fn] serialize-fn)]
      (is (= "schrubles"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:options :body])))))

  (testing "override extension"
    (let [extension {cs/EpochMillis #(.toEpochMilli %)}
          serialize-fn #(json/json->string %1 %2 %3)
          context (-> context
                      (assoc-in [:overrides :extend-serialization] extension)
                      (assoc-in [:overrides :json-serializer :serialize-fn] serialize-fn)
                      (assoc-in [:options :body] {:created-at #epoch 1422554400000})
                      (assoc-in [:endpoint] :service/bye))]
      (is (= "{\"created_at\":1422554400000}"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:options :body]))))))
