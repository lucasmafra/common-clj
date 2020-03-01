(ns common-clj.http-server.interceptors.content-type-test
  (:require [clojure.test :refer :all]
            [common-clj.http-server.interceptors.content-type :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(def context {})

(deftest content-type
  (testing "add application/json to content-type header"
    (is (= {"Content-Type" "application/json"}
           (get-in
            (chain/execute context [nut/content-type])
            [:response :headers])))))
