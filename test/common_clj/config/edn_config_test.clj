(ns common-clj.config.edn-config-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.config.edn-config :as edn-config]
            [common-clj.config.protocol :as config-protocol])
  (:import clojure.lang.ExceptionInfo))

(def valid-config (io/resource "fixtures/app.edn"))
(def invalid-config (io/resource "fixtures/invalid_app.edn"))

(deftest get-config
  (testing "reads config from edn file"
    (is (= {:app-name :common-clj}
           (with-redefs [io/resource (constantly valid-config)]
             (config-protocol/get-config
              (component/start (edn-config/new-config)))))))

  (testing "throws on invalid config"
    (is (thrown? ExceptionInfo
                 (with-redefs [io/resource (constantly invalid-config)]
                   (component/start (edn-config/new-config))))))

  (testing "throws when does not find config file"
    (is (thrown-with-msg? ExceptionInfo #"Make sure you have your app config"
                          (with-redefs [io/resource (constantly nil)]
                            (component/start (edn-config/new-config)))))))

(deftest get-env
  (testing "defaults to prod"
    (is (= :prod
           (with-redefs [io/resource (constantly valid-config)]
             (config-protocol/get-env
              (component/start (edn-config/new-config)))))))

  (testing "can be overwritten"
    (is (= :test
           (with-redefs [io/resource (constantly valid-config)]
             (config-protocol/get-env
              (component/start (edn-config/new-config :test))))))))
