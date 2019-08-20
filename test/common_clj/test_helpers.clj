(ns common-clj.test-helpers
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.consumer.protocol :as consumer.protocol]))

(defn init!
  "setup components and store them in the world"
  [system-map world]
  (let [system (component/start system-map)]
    (assoc world :system system)))

(defn message-arrived!
  [topic message world]
  (let [consumer (-> world :system :consumer)]
    (consumer.protocol/consume! consumer topic message)
    world))

(defn try-consume!
  "Try to consume message and, if any error is thrown while consuming,
  add the error to world in path [:consumption-errors <topic>]"
  [topic message world]
  (try
    (message-arrived! topic message world)
    (catch Exception e
      (update-in world [:consumption-errors topic] (partial cons e)))))

(defn schema-error? [exception-info]
  (-> exception-info
      ex-data
      :type
      (= :schema.core/error)))
