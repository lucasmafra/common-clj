(ns common-clj.http-server.interceptors.json-serializer-test
  (:require [clojure.test :refer :all]
            [common-clj.http-server.interceptors.json-serializer :as nut]
            [common-clj.json :as json]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain])
  (:import clojure.lang.ExceptionInfo))

(def context
  {:response {:body {:age 25}}
   :route    {:route-name :route/hello}
   :routes   {:route/hello {:response-schema {:age cs/PosInt}}
              :route/bye   {:response-schema {:created-at cs/EpochMillis}}}})

(deftest json-serializer
  (testing "serializes response to json"
    (is (= "{\"age\":25}"
           (get-in
            (chain/execute context [nut/json-serializer])
            [:response :body]))))

  (testing "throws when response does not conform to schema"
    (let [context (assoc-in context [:response :body :age] "not an positive integer")]
      (is (thrown-with-msg? ExceptionInfo #"Value does not match schema"
                            (chain/execute context [nut/json-serializer])))))

  (testing "override serialize-fn"
    (let [serialize-fn (fn [_ _ _] "schrubles")
          context      (assoc-in context [:overrides :json-serializer :serialize-fn] serialize-fn)]
      (is (= "schrubles"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:response :body])))))

  (testing "override extension"
    (let [extension    {cs/EpochMillis #(.toEpochMilli %)}
          serialize-fn #(json/json->string %1 %2 %3)
          context      (-> context
                           (assoc-in [:overrides :extend-serialization] extension)
                           (assoc-in [:overrides :json-serializer :serialize-fn] serialize-fn)
                           (assoc-in [:response :body] {:created-at #epoch 1422554400000})
                           (assoc-in [:route :route-name] :route/bye))]
      (is (= "{\"created_at\":1422554400000}"
             (get-in
              (chain/execute context [nut/json-serializer])
              [:response :body]))))))
