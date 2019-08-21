(ns common-clj.components.logger.in-memory-logger
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.components.logger.protocol :refer [Logger]]))

(s/defrecord InMemoryLogger []
  component/Lifecycle
  (start [component]
    (assoc component :logs (atom {})))

  (stop [component]
    (assoc component :logs nil))
  
  Logger
  (log! [component tag value]
    (-> component
        :logs
        (swap! (fn [logs] (update logs tag #(conj % value))))))

  (get-logs [component tag]
    (-> component
        :logs
        deref
        (get tag))))

(defn new-logger []
  (map->InMemoryLogger {}))
