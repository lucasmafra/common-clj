(ns common-clj.components.docstore-client.dynamo-docstore-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.coercion :refer [coerce]]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.docstore-client.protocol :refer [DocstoreClient]
             :as docstore-client.protocol]
            [schema.core :as s]
            [taoensso.faraday :as far]))

(defn- assoc-if [m k v] (if (seq v) (assoc m k v) m))

(defn- ensure-table!
  [client-options dynamo-tables]
  (doseq [[table-name {:keys [primary-key secondary-key]}] dynamo-tables]
    (far/ensure-table client-options
                      table-name
                      primary-key
                      {:range-keydef secondary-key})))

(defn- to-dynamo-item
  [item {:keys [primary-key secondary-key]}]
  (-> item
      (select-keys [primary-key secondary-key])
      (assoc :dynamo-data (far/freeze item))))

(defn- from-dynamo-item
  [dynamo-item]
  (-> dynamo-item
      (select-keys [:dynamo-data])
      (map [:dynamo-data])
      first))

(defn- extract-relevant-keys
  [schema item]
  (if (and schema
           (not= schema s/Any))
    (select-keys )))

(s/defrecord DynamoDocstoreClient []
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [{:keys [dynamo-endpoint aws-access-key aws-secret-key dynamo-tables]}
          (config.protocol/get-config config)
          client-options {:endpoint   dynamo-endpoint
                          :access-key aws-access-key
                          :secret-key aws-secret-key}]
      (ensure-table! client-options dynamo-tables)
      (assoc component :client-options client-options)))

  (stop [component]
    (assoc component :client-options nil))

  DocstoreClient
  (put-item! [{:keys [client-options config] :as component} table-name v]
    (let [{:keys [dynamo-tables]}             (config.protocol/get-config config)
          {:keys [primary-key secondary-key]} (get dynamo-tables table-name)
          [primary-key-name]                  primary-key
          [secondary-key-name]                secondary-key
          primary-key-value                   (get v primary-key-name)
          secondary-key-value                 (get v secondary-key-name)]
      (docstore-client.protocol/put-item!
       component
       table-name
       (assoc-if {primary-key-name primary-key-value}
                 secondary-key-name
                 secondary-key-value)
       v)))
  (put-item! [{:keys [client-options config]} table-name k v]
    (let [{:keys [dynamo-tables]}             (config.protocol/get-config config)
          {:keys [primary-key secondary-key]} (get dynamo-tables table-name)
          [primary-key-name]                  primary-key
          [secondary-key-name]                secondary-key
          primary-key-value                   (or (get v primary-key-name)
                                                  (get k primary-key-name))
          secondary-key-value                 (or (get v secondary-key-name)
                                                  (get k secondary-key-name))
          table-defined?                      (not (nil? primary-key))]
      (when-not table-defined?
        (throw (ex-info "Can't do operations on non-existent table"
                        {:type :non-existent-table})))
      (when-not primary-key-value
        (throw (ex-info "Missing primary key"
                        {:table table-name
                         :type  :missing-primary-key})))

      (when (and secondary-key (not secondary-key-value))
        (throw (ex-info "Missing secondary key"
                        {:table table-name
                         :type  :missing-secondary-key})))
      (try
        (let [item (-> v
                       (assoc
                        primary-key-name
                        (str primary-key-value))
                       (assoc-if
                        secondary-key-name
                        (str secondary-key-value))
                       (to-dynamo-item {:primary-key   primary-key-name
                                        :secondary-key secondary-key-name}))]
          (far/put-item client-options table-name item))
        (catch com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException e
          (throw (ex-info "Can't do operations on non-existent table"
                          {:type :non-existent-table}))))
      v))

  (maybe-get-item [component table-name k options]
    (try
      (docstore-client.protocol/get-item component table-name k options)
      (catch Exception e
        (when-not (= :not-found (-> e ex-data :type))
          (throw e)))))

  (get-item [{:keys [client-options config]} table-name item-key {:keys [schema-resp]}]
    (let [{:keys [dynamo-tables]}             (config.protocol/get-config config)
          {:keys [primary-key secondary-key]} (get dynamo-tables table-name)
          [primary-key-name]                  primary-key
          [secondary-key-name]                secondary-key
          primary-key-value                   (get item-key primary-key-name)
          secondary-key-value                 (get item-key secondary-key-name)
          table-defined?                      (not (nil? primary-key))]
      (when-not table-defined?
        (throw (ex-info "Can't do operations on non-existent table"
                        {:type :non-existent-table})))
      (when-not primary-key-value
        (throw (ex-info "Missing primary key"
                        {:table table-name
                         :type  :missing-primary-key})))

      (when (and secondary-key (not secondary-key-value))
        (throw (ex-info "Missing secondary key"
                        {:table table-name
                         :type  :missing-secondary-key})))
      (try
        (let [result (->> (map (fn [[k v]] [k (str v)]) item-key)
                          (into {})
                          (far/get-item client-options table-name)
                          from-dynamo-item)]
          (when-not result
            (throw (ex-info "Not found" {:type :not-found})))
          (coerce schema-resp result {:allow-extra-keys true}))
        
        (catch com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException e
          (throw (ex-info "Can't do operations on non-existent table"
                          {:type :non-existent-table}))))))

  (query [{:keys [client-options config]} table-name key-conditions {:keys [schema-resp]}]
    (let [{:keys [dynamo-tables]}             (config.protocol/get-config config)
          {:keys [primary-key secondary-key]} (get dynamo-tables table-name)
          [primary-key-name]                  primary-key
          [secondary-key-name]                secondary-key
          primary-key-value                   (get key-conditions primary-key-name)
          [cmp-fn cmp-arg]                    (get key-conditions secondary-key-name)
          table-defined?                      (not (nil? primary-key))]
      (when-not table-defined?
        (throw (ex-info "Can't do operations on non-existent table"
                        {:type :non-existent-table})))
      (when-not primary-key-value
        (throw (ex-info "Missing primary key"
                        {:table table-name
                         :type  :missing-primary-key})))
      (try
        (->> {primary-key-name [:eq (str primary-key-value)]}
             (far/query client-options
                        table-name)
             (map from-dynamo-item)
             (#(coerce schema-resp % {:allow-extra-keys true})))
        (catch com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException e
          (throw (ex-info "Can't do operations on non-existent table"
                          {:type :non-existent-table})))))))

(defn new-docstore-client []
  (map->DynamoDocstoreClient {}))
