(ns common-clj.coercion-test
  (:require [clojure.test :refer [deftest is testing]]
            [common-clj.coercion :as nut]
            [common-clj.schema.core :as cs]
            [schema.core :as s]))

(deftest local-date
  (testing "coerces"
    (is (= #local-date "2012-02-12"
           (nut/coerce cs/LocalDate "2012-02-12")))))

(deftest local-date-time
  (testing "coerces"
    (is (= #local-date-time "2012-02-12T20:00:00"
           (nut/coerce cs/LocalDateTime "2012-02-12T20:00:00")))))

(deftest enum
  (testing "coerces"
    (is (= :a
           (nut/coerce (s/enum :a :b) "a"))))

  (testing "accepts uppercase and underscore"
    (is (= :value-a
           (nut/coerce (s/enum :value-a :value-b) :VALUE_A)))))
