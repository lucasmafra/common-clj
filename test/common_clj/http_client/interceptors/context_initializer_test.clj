(ns common-clj.http-client.interceptors.context-initializer-test
  (:require [common-clj.http-client.interceptors.context-initializer :as nut]
            [clojure.test :refer :all]
            [io.pedestal.interceptor.chain :as chain]))

(def context {})

(deftest context-initializer
  (testing "injects endpoints to context"
    (is (= :dummy
           (get-in
            (chain/execute context [(nut/context-initializer {:endpoints :dummy})])
            [:endpoints]))))

  (testing "injects endpoint to context"
    (is (= :dummy
           (get-in
            (chain/execute context [(nut/context-initializer {:endpoint :dummy})])
            [:request :endpoint]))))

  (testing "injects options to context"
    (is (= :dummy
           (get-in
            (chain/execute context [(nut/context-initializer {:options :dummy})])
            [:request :options]))))

  (testing "injects config to context"
    (is (= :dummy
           (get-in
            (chain/execute context [(nut/context-initializer {:config :dummy})])
            [:config]))))

  (testing "injects overrides to context"
    (is (= :dummy
           (get-in
            (chain/execute context [(nut/context-initializer {:overrides :dummy})])
            [:overrides])))))
