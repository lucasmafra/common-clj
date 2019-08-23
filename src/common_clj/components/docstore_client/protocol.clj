(ns common-clj.components.docstore-client.protocol)

(defprotocol DocstoreClient
  (ensure-table! [component table-name primary-key-schema]
                 [component table-name primary-key-schema secondary-key-schema])
  (delete-table! [component table-name])
  (put-item! [component table-name item])
  (get-item [component table-name item-key])
  (query [component table-name primary-key-conditions]))
