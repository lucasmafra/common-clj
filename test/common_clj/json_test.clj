(ns common-clj.json-test
  (:require [common-clj.json :as json]
            [midje.sweet :refer :all]))


(facts "json->string"
  (fact "string"
    (json/json->string {:a "John"})
    => "{\"a\":\"John\"}")

  (fact "LocalDate"
    (json/json->string {:a #date "2019-08-22"})
    => "{\"a\":\"2019-08-22\"}")

  (fact "LocalDateTime"
    (json/json->string {:a {:b #date-time "2019-08-22T09:52:37"}})
    => "{\"a\":{\"b\":\"2019-08-22T09:52:37\"}}")

  (fact "BigDecimal"
    (json/json->string {:a 200M})
    => "{\"a\":200}"))

(fact "string->json"
  (-> {:a "John"}
      json/json->string
      json/string->json)
  => {:a "John"})
