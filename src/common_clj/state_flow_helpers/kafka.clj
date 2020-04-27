(ns common-clj.state-flow-helpers.kafka
  (:require [common-clj.clojure-test-helpers.producer :as producer]
            [state-flow.state :as state])
  (:import org.apache.kafka.clients.producer.ProducerRecord))

(defn message-arrived! [topic message]
  (state/gets
   (fn [world]
     (let [produced-records (-> world :system :producer :produced-records)
           record  (new ProducerRecord topic 0  message)]
       (swap! produced-records update :records conj record)))))

(defn get-produced-messages [topic]
  (state/gets
   (fn [world]
     (producer/get-produced-messages topic (-> world :system :producer)))))
