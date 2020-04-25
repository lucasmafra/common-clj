(ns common-clj.key-value-store.protocol)

(defprotocol KeyValueStore
  (fetch [component k])

  (store! [component k v]))
