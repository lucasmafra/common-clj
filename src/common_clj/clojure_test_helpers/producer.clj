(ns common-clj.clojure-test-helpers.producer)

(defn get-produced-messages [topic producer]
  (->> producer
       :kafka-client
       .history
       (filter #(= topic (.topic %)))
       (mapv #(.value %))))
