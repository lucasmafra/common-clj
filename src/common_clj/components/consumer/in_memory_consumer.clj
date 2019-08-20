(ns common-clj.components.consumer.in-memory-consumer
  (:require [common-clj.components.consumer.protocol]
            [common-clj.schemata.consumer :as schemata.consumer]
            [common-clj.components.consumer.protocol :refer [Consumer]]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(defn subscription-key [topic handler]
  (str topic "-" (clojure.repl/demunge (str handler))))

(defn maybe-call [handler schema handler-topic component]
  (fn [_ _ _ {:keys [message topic]}]
    (if (= topic handler-topic)
      (do
        (s/validate schema message)
        (handler message component)))))

(s/defrecord InMemoryConsumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  component/Lifecycle
  (start [component]
    (let [channel (atom nil)]
      (doseq [[topic {:keys [handler schema]}] consumer-topics]
        (add-watch channel
                   (subscription-key topic handler)
                   (maybe-call handler schema topic component)))
      (assoc component :channel channel)))
  
  (stop [{:keys [consumer] :as component}])
  
  Consumer
  (consume! [component topic message]
    (let [channel (-> component :channel)]
      (reset! channel {:topic topic :message message}))))

(s/defn new-consumer [consumer-topics :- schemata.consumer/ConsumerTopics]
  (map->InMemoryConsumer {:consumer-topics consumer-topics}))
