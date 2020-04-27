(ns common-clj.kafka.consumer.interceptors.mock-consumer-loop
  (:require [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain])
  (:import org.apache.kafka.clients.consumer.ConsumerRecord
           org.apache.kafka.common.TopicPartition))

(defn- add-records! [records {:keys [kafka-client]}]
  (let [partitions (map #(new TopicPartition (.topic %) 0) records)]
    (.rebalance kafka-client partitions)
    (.updateBeginningOffsets kafka-client (reduce (fn [acc p] (assoc acc p 0)) {} partitions))
    (.seekToBeginning kafka-client partitions)
    (doseq [record records] (.addRecord kafka-client record))))

(defn clear-pedestal-keys [context]
  (apply dissoc context [:io.pedestal.interceptor.chain/execution-id
                         :io.pedestal.interceptor.chain/queue
                         :io.pedestal.interceptor.chain/stack]))

(defn- poll-and-consume! [{:keys [kafka-client consume-interceptors] :as context}]
  (let [records (seq (.poll kafka-client Long/MAX_VALUE))]
    (doseq [record records]
      (let [initial-context (-> context (assoc :record record) clear-pedestal-keys)]
        (chain/execute initial-context consume-interceptors)))))

(defn not-in-coll? [coll] (fn [v] (nil? (first (filter #(= v %) coll)))))

(defn- new-records [records-before records-after]
  (filter (not-in-coll? records-before) records-after))

(defn- producer-record->consumer-record [i record]
  (new ConsumerRecord (.topic record) 0 i (.key record) (.value record)))

(defn- watcher [context]
  (fn [_ _ old-state new-state]
    (let [producer-records (new-records (:records old-state) (:records new-state))
          consumer-records (map-indexed producer-record->consumer-record producer-records)]
      (add-records! consumer-records context)
      (poll-and-consume! context))))

(def i-consumer-loop
  :common-clj.kafka.consumer.interceptors.consumer-loop/consumer-loop)

(def mock-consumer-loop
  (interceptor/interceptor
   {:name  ::mock-consumer-loop
    :enter (fn [{:keys [:io.pedestal.interceptor.chain/queue components] :as context}]
             (let [produced-records (-> components :producer :produced-records)
                   modified-queue (->> queue
                                       (remove #(= i-consumer-loop (:name %)))
                                       (into clojure.lang.PersistentQueue/EMPTY))]
               (add-watch produced-records :produced-records (watcher context))
               (assoc context :io.pedestal.interceptor.chain/queue modified-queue)))}))
