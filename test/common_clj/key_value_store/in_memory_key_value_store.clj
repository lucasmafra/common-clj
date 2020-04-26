(ns common-clj.key-value-store.in-memory-key-value-store
  (:require [com.stuartsierra.component :as component]
            [common-clj.key-value-store.protocol :as kvs-pro]))

(defrecord InMemoryKeyValueStore []
  component/Lifecycle
  (start [component]
    (assoc component :store (atom {})))

  (stop [component]
    (dissoc component :store))

  kvs-pro/KeyValueStore
  (fetch [{:keys [store]} k]
    (-> store deref k))

  (store! [{:keys [store]} k v]
    (swap! store assoc k v)))

(defn new-key-value-store []
  (map->InMemoryKeyValueStore {}))
