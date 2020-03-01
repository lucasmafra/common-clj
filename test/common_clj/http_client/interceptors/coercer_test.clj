(ns common-clj.http-client.interceptors.coercer-test
  (:require [clojure.test :refer :all]
            [common-clj.coercion :refer [pos-int-matcher]]
            [common-clj.http-client.interceptors.coercer :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(def ResponseSchema
  {:message s/Str
   :amount cs/PosInt})

(def context
  {:endpoints
   {:service/hello
    {:response-schema ResponseSchema}}

   :endpoint :service/hello

   :response
   {:body {:message "Bla"
           :amount  "5"}}})

(deftest coercer
  (testing "coerce valid response"
    (is (= {:message "Bla"
            :amount  5}
           (get-in (chain/execute context [nut/coercer])
                   [:response :body]))))

  (testing "throws when invalid response"
    (let [context (assoc-in context [:response :body :amount] "-5")]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/coercer])))))

  (testing "override :coercers"
    (let [custom-coercers {} ; empty, doesn't know how to coerce cs/PosInt
          context         (-> context
                              (assoc-in [:overrides :coercer :coercers] custom-coercers))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/coercer])))))

  (testing "override :extension - extends coercion map"
    (let [custom-coercers {}                          ; empty, doesn't know how to coerce cs/PosInt
          extension       {cs/PosInt pos-int-matcher} ; adds cs/PosInt to coercion map
          context         (-> context
                              (assoc-in [:overrides :coercer :coercers] custom-coercers)
                              (assoc-in [:overrides :extend-coercion] extension))]
      (is (= {:message "Bla"
              :amount    5}
             (get-in (chain/execute context [nut/coercer])
                     [:response :body]))))))
