(ns common-clj.components.docstore-client.protocol)

(defprotocol DocstoreClient
  (put-item!
    [component table-name v]
    [component table-name k v])

  (get-item [component table-name k options])

  (query [component table-name primary-key-conditions options]))
