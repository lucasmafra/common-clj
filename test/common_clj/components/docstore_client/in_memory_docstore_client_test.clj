(ns in-memory-docstore-client-test
  (:require [midje.sweet :refer :all]
            [common-clj.components.docstore-client.in-memory-docstore-client
             :as in-memory-docstore-client]
            [common-clj.components.docstore-client.protocol :as
             docstore-client.protocol]
            [com.stuartsierra.component :as component]
            [common-clj.test-helpers :refer [schema-error? throws-ex]]
            [common-clj.components.config.in-memory-config
             :as in-memory-config]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

(def dynamo-tables
  {:table-a {:primary-key [:id :s]}

   :table-b {:primary-key   [:employee-id :s]
             :secondary-key [:control-key :s]}})

(def config {:app-name :common-clj
             :dynamo-tables dynamo-tables})

(def system
  (component/system-map
   :config (in-memory-config/new-config config)

   :db     (component/using
            (in-memory-docstore-client/new-docstore-client)
            [:config])))

(defn init-docstore-client []
  (-> system component/start :db))

(def SchemaA
  {:id        s/Uuid
   :date      java.time.LocalDate
   :date-time java.time.LocalDateTime
   :amount    java.math.BigDecimal
   :boolean   s/Bool
   :vec       [s/Int]
   :nested    {:a s/Int
               :b {:c s/Str
                   :d s/Keyword
                   :f [s/Str]}}})

(s/def item-a :- SchemaA
  {:id        #uuid "e0515b09-5e4f-4af2-a879-59c5cdbbd00a"
   :date      #date "2019-08-22"
   :date-time #date-time "2019-08-22T12:00:00"
   :amount    20M
   :boolean   true
   :vec       [1 2 3]
   :nested    {:a 1
               :b {:c "2"
                   :d :e
                   :f ["g" "h" "i"]}}})

(s/with-fn-validation
  (facts "put-item!"
    (fact "can't do operations on non-existent table"
      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :unknown-table
                                              {:id   "dont care"
                                               :name "dont care"}))
      => (throws-ex {:type :non-existent-table}))

    (fact "Missing primary key"
      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-a
                                              {:name "dont care"}))
      => (throws-ex {:type :missing-primary-key}))

    (fact "missing secondary key"
      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-b
                                              {:employee-id "dont care"}))
      => (throws-ex {:type :missing-secondary-key}))

    (fact "accepts multiple arity"
      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-a
                                              {:id   "dont care"
                                               :name "dont care"}))
      => {:id   "dont care"
          :name "dont care"}

      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-a
                                              {:id "dont care"}
                                              {:name "dont care"}))
      => {:name "dont care"}

      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-b
                                              {:employee-id "dont care"
                                               :control-key "dont care"
                                               :name        "dont care"}))
      => {:employee-id "dont care"
          :control-key "dont care"
          :name        "dont care"}

      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-b
                                              {:employee-id "dont care"
                                               :control-key "dont care"}
                                              {:name "dont care"}))
      => {:name "dont care"}

      (-> (init-docstore-client)
          (docstore-client.protocol/put-item! :table-b
                                              {:employee-id "dont care"}
                                              {:control-key "dont care"
                                               :name        "dont care"}))
      => {:control-key "dont care"
          :name        "dont care"}))

  (facts "get-item"
    (fact "can't do operations on non existent table"
      (-> (init-docstore-client)
          (docstore-client.protocol/get-item :unknown-table
                                             {:id "dont care"}
                                             {:schema-resp s/Any}))
      => (throws-ex {:type :non-existent-table}))

    (fact "missing primary key"
      (-> (init-docstore-client)
          (docstore-client.protocol/get-item :table-a
                                             {}
                                             {:schema-resp s/Any}))
      => (throws-ex {:type :missing-primary-key}))

    (fact "missing secondary key"
      (-> (init-docstore-client)
          (docstore-client.protocol/get-item :table-b
                                             {:employee-id "dont care"}
                                             {:schema-resp s/Any}))
      => (throws-ex {:type :missing-secondary-key}))

    (fact "retrieves item"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item! docstore-client
                                            :table-a
                                            {:id   "123"
                                             :name "dont care"})
        (docstore-client.protocol/get-item docstore-client
                                           :table-a
                                           {:id "123"}
                                           {:schema-resp s/Any}))
      => {:id   "123"
          :name "dont care"}

      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item! docstore-client
                                            :table-b
                                            {:employee-id "123"
                                             :control-key "abc"
                                             :name        "dont care"})
        (docstore-client.protocol/get-item docstore-client
                                           :table-b
                                           {:employee-id "123"
                                            :control-key "abc"}
                                           {:schema-resp s/Any}))
      => {:employee-id "123"
          :control-key "abc"
          :name        "dont care"})

    (fact "coerces response"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-a
         item-a)
        (docstore-client.protocol/get-item
         docstore-client
         :table-a
         {:id #uuid "e0515b09-5e4f-4af2-a879-59c5cdbbd00a"}
         {:schema-resp SchemaA}))
      => item-a)
    (fact "throws schema-error when response does not match"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-a
         (assoc item-a :id "invalid uuid"))
        (docstore-client.protocol/get-item
         docstore-client
         :table-a
         {:id "invalid uuid"}
         {:schema-resp SchemaA}))
      => (throws-ex {:type :schema.core/error})))

  (facts "query"
    (fact "can't do operations on non-existent table"
      (-> (init-docstore-client)
          (docstore-client.protocol/query :unknown-table
                                          {:id "dont care"}
                                          {:schema-resp s/Any}))
      => (throws-ex {:type :non-existent-table}))

    (fact "missing primary key"
      (-> (init-docstore-client)
          (docstore-client.protocol/query :table-b
                                          {}
                                          {:schema-resp s/Any}))
      => (throws-ex {:type :missing-primary-key}))

    (fact "query items"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-a
         {:id   "123"
          :name "dont care"})
        (docstore-client.protocol/query
         docstore-client
         :table-a
         {:id "123"}
         {:schema-resp s/Any}))
      => [{:id   "123"
           :name "dont care"}]

      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-b
         {:employee-id "123"
          :control-key "dont care"
          :name        "dont care"})
        (docstore-client.protocol/query
         docstore-client
         :table-b
         {:employee-id "123"}
         {:schema-resp s/Any}))
      => [{:employee-id "123"
           :control-key "dont care"
           :name        "dont care"}])

    (fact "coerces response"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-a
         item-a)
        (docstore-client.protocol/query
         docstore-client
         :table-a
         {:id #uuid "e0515b09-5e4f-4af2-a879-59c5cdbbd00a"}
         {:schema-resp [SchemaA]}))
      => [item-a])

    (fact "throws schema-error when response does not match"
      (let [docstore-client (init-docstore-client)]
        (docstore-client.protocol/put-item!
         docstore-client
         :table-a
         (assoc item-a :id "invalid uuid"))
        (docstore-client.protocol/query
         docstore-client
         :table-a
         {:id "invalid uuid"}
         {:schema-resp [SchemaA]}))
      => (throws-ex {:type :schema.core/error}))))
