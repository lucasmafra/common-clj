(ns common-clj.state-flow-helpers.producer
  (:require [common-clj.clojure-test-helpers.producer :as producer-helpers]
            [state-flow.state :as state]))

(defn get-produced-messages [topic]
  (state/gets
   (fn [{{:keys [producer]} :system}]
     (assert (some? producer) "No producer found in the system")
     (producer-helpers/get-produced-messages topic producer))))
