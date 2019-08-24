(ns common-clj.components.docstore-client.dynamo-docstore-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.docstore-client.protocol :refer [DocstoreClient]]
            [schema.core :as s]
            [taoensso.faraday :as far]))

(s/defrecord DynamoDocstoreClient []
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [{:keys [dynamo-endpoint aws-access-key aws-secret-key]}
          (config.protocol/get-config config)
          client-options {:endpoint   dynamo-endpoint
                          :access-key aws-access-key
                          :secret-key aws-secret-key}]
      (assoc component :client-options client-options)))

  (stop [component]
    (assoc component :client-options nil))

  DocstoreClient
  (ensure-table! [{:keys [client-options]} table-name primary-key-schema]
    (far/ensure-table
     client-options
     table-name
     primary-key-schema))

  (ensure-table! [{:keys [client-options]} table-name primary-key-schema
                  secondary-key-schema]
    (far/ensure-table
     client-options
     table-name
     primary-key-schema
     {:range-keydef
      secondary-key-schema}))

  (delete-table! [{:keys [client-options]} table-name]
    (far/delete-table
     client-options
     table-name))

  (put-item! [{:keys [client-options]} table-name item]
    (far/put-item client-options table-name item))

  (get-item [{:keys [client-options]} table-name item-key]
    (far/get-item client-options table-name item-key))

  (query [{:keys [client-options]} table-name primary-key-conditions]
    (far/query client-options table-name primary-key-conditions)))

(defn new-docstore-client []
  (map->DynamoDocstoreClient {}))
