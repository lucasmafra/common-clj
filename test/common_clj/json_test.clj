(ns common-clj.json-test
  (:require [midje.sweet :refer :all]
            [common-clj.json :as json]))


(facts "json->string"
  (fact "string"
    (json/json->string {:a "John"})
    => "{\"a\":\"John\"}")

  (fact "date"
    (json/json->string {:a #date "2019-08-22"})
    => "{\"a\":\"2019-08-22\"}")

  (fact "date-time"
    (json/json->string {:a #date-time "2019-08-22T09:52:37"})
    => "{\"a\":\"2019-08-22T12:52:37Z\"}"
    (provided
     (json/zone) => (java.time.ZoneId/of "America/Sao_Paulo"))))


