(ns common-clj.kafka.consumer.interceptors.coercer-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]
            [common-clj.coercion :refer [local-date-matcher]]
            [common-clj.kafka.consumer.interceptors.coercer :as nut]
            [common-clj.schema.core :as cs]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo
           org.apache.kafka.clients.consumer.ConsumerRecord))

(def Schema
  {:name          s/Str
   :date-of-birth cs/LocalDate})

(def context
  {:topics
   {:topic/a
    {:topic  "TOPIC_A"
     :schema Schema}}

   :record (new ConsumerRecord "TOPIC_A" 0 0 0 #json {"name"          "John Doe"
                                                      "date-of-birth" "1995-02-08"})

   :message
   {:name          "John Doe"
    :date-of-birth "1995-02-08"}})

(deftest coercer
  (testing "coerce valid response"
    (is (= {:name          "John Doe"
            :date-of-birth #local-date "1995-02-08"}
           (get-in (chain/execute context [nut/coercer])
                   [:message]))))

  (testing "throws when invalid response"
    (let [context (assoc-in context [:message :date-of-birth] "not a local date")]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/coercer])))))

  (testing "override :coercers"
    (let [custom-coercers {} ; empty, doesn't know how to coerce cs/LocalDate
          context         (assoc-in context [:overrides :coercer :coercers] custom-coercers)]
      (is (thrown? ExceptionInfo
                   (chain/execute context [nut/coercer])))))

  (testing "override :extension - extends coercion map"
    (let [custom-coercers {}
          extension       {cs/LocalDate local-date-matcher} ; adds cs/LocalDate to coercion map
          context         (-> context
                              (assoc-in [:overrides :coercer :coercers] custom-coercers)
                              (assoc-in [:overrides :coercer :extend-coercion] extension))]
      (is (= {:name          "John Doe"
              :date-of-birth #local-date "1995-02-08"}
             (get-in (chain/execute context [nut/coercer])
                     [:message]))))))
