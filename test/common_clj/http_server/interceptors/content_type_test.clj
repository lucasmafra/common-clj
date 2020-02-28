(ns common-clj.http-server.interceptors.content-type-test
  (:require [common-clj.http-server.interceptors.content-type :as nut]
            [clojure.test :refer :all]
            [io.pedestal.interceptor.chain :as chain]))

(def context {})

(deftest content-type
  (testing "add application/json to content-type header"
    (is (= {"Content-Type" "application/json"}
           (get-in
            (chain/execute context [nut/content-type])
            [:response :headers])))))
