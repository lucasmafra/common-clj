(ns common-clj.components.consumer.in-memory-consumer
  (:require [clojure.repl :refer [demunge]]
            [com.stuartsierra.component :as component]
            [common-clj.components.consumer.protocol :refer [Consumer]]
            [common-clj.schemata.consumer :as schemata.consumer]
            [schema.core :as s]))

(defn subscription-key [topic handler]
  (str topic "-" (demunge (str handler))))

(defn maybe-call [handler schema handler-topic component]
  (fn [_ _ _ {:keys [message topic]}]
    (when (= topic handler-topic) (s/validate schema message) (handler message component))))

(s/defrecord InMemoryConsumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  component/Lifecycle
  (start [component]
    (let [channel (atom nil)]
      (doseq [[topic {:keys [topic/handler topic/schema]}] consumer-topics]
        (add-watch channel
                   (subscription-key topic handler)
                   (maybe-call handler schema topic component)))
      (assoc component :channel channel)))
  
  (stop [{:keys [consumer] :as component}])
  
  Consumer
  (consume! [component topic message]
    (let [channel (:channel component)]
      (reset! channel {:topic topic :message message}))))

(s/defn new-consumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  (map->InMemoryConsumer {:consumer-topics consumer-topics}))
