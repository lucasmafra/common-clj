(ns common-clj.http-server.interceptors.body-coercer-test
  (:require [clojure.test :refer :all]
            [common-clj.coercion :refer [pos-int-matcher]]
            [common-clj.http-server.interceptors.body-coercer :as nut]
            [common-clj.schema :as cs]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(def RequestSchema
  {:color (s/enum :blue :green)
   :age   cs/PosInt})

(def context
  {:request
   {:json-params {:color "blue"
                  :age   "25"}}

   :route {:route-name :route/hello}

   :routes {:route/hello {:request-schema RequestSchema}}})

(deftest body-coercer
  (testing "coerces request body"
    (is (= {:color :blue
            :age   25}
           (get-in
            (chain/execute context [nut/body-coercer])
            [:request :body]))))

  (testing "throws error when req body does not conform to schema"
    (let [context (assoc-in context [:request :json-params :color] "blues")]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/body-coercer])))))

  (testing "throws when there's request body but no schema"
    (let [context (-> context
                      (assoc-in [:routes :route/hello :request-schema] nil))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/body-coercer])))))

  (testing "does not throw when there's no request schema nor body"
    (let [context (-> context
                      (assoc-in [:request :json-params] nil)
                      (assoc-in [:routes :route/hello :request-schema] nil))]
      (is (= nil
             (get-in
              (chain/execute context [nut/body-coercer])
              [:request :body])))))

  (testing "override :coercers"
    (let [custom-coercers {} ; empty, doesn't know how to coerce cs/PosInt
          context         (-> context
                              (assoc-in [:overrides :body-coercer :coercers] custom-coercers))]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/body-coercer])))))

  (testing "override :extension - extends coercion map"
    (let [custom-coercers {}                          ; empty, doesn't know how to coerce cs/PosInt
          extension       {cs/PosInt pos-int-matcher} ; adds cs/PosInt to coercion map
          context         (-> context
                              (assoc-in [:overrides :body-coercer :coercers] custom-coercers)
                              (assoc-in [:overrides :body-coercer :extension] extension))]
      (is (= {:color :blue
              :age   25}
             (get-in (chain/execute context [nut/body-coercer])
                     [:request :body]))))))
