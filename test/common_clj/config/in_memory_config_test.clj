(ns common-clj.config.in-memory-config-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.config.in-memory-config :as nut]
            [common-clj.config.protocol :as config-protocol])
  (:import clojure.lang.ExceptionInfo))

(def valid-config {:app/name :common-clj})
(def invalid-config {"APP_NAME" "INVALID_APP_CONFIG"})

(deftest get-config
  (testing "loads given config"
    (is (= {:app/name :common-clj}
           (config-protocol/get-config
            (component/start (nut/new-config valid-config))))))

  (testing "throws on invalid config"
    (is (thrown? ExceptionInfo
                 (component/start (nut/new-config invalid-config))))))

(deftest get-env
  (testing "defaults to prod"
    (is (= :prod
           (config-protocol/get-env
            (component/start (nut/new-config valid-config))))))

  (testing "can be overwritten"
    (is (= :test
           (config-protocol/get-env
            (component/start (nut/new-config valid-config :test)))))))
