(ns common-clj.components.counter.in-memory-counter
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.counter.protocol :refer [Counter]]))

(defrecord InMemoryCounter []
  component/Lifecycle
  (start [component]
    (assoc component :counter (atom 0)))

  (stop [component]
    (assoc component :counter nil))

  Counter
  (inc! [component]
    (-> component
        :counter
        (swap! inc)))

  (get-count [component]
    (-> component
        :counter
        deref)))

(defn new-counter [] (map->InMemoryCounter {}))
