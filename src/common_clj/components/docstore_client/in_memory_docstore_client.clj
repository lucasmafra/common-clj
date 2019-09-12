(ns common-clj.components.docstore-client.in-memory-docstore-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.docstore-client.protocol
             :refer [DocstoreClient] :as docstore-client.protocol]
            [schema.core :as s]
            [common-clj.components.config.protocol :as config.protocol]))

(defn- assoc-if
  [m k v]
  (if v (assoc m k v) m))

(def ^:private cmp->cmp-fn
  "Maps comparison-operators symbols to comparison functions
   Symbols yet to support:
    #{:le :lt :ge :gt :begins-with :between :ne
      :not-null :null :contains :not-contains :in}"
  {:eq =})

(defn- init-store [dynamo-tables]
  (reduce
   (fn [acc [table-name {:keys [primary-key secondary-key]}]]
     (let [schema (assoc-if {:primary-key primary-key}
                            :secondary-key
                            secondary-key)]
       (assoc acc table-name {:schema schema
                              :data {}})))
   {}
   dynamo-tables))

(s/defrecord InMemoryDocstoreClient []
  component/Lifecycle
  (start [{:keys [config] :as component}]
    (let [{:keys [dynamo-tables]} (config.protocol/get-config config)]
      (assoc component :store (atom (init-store dynamo-tables)))))

  (stop [component]
    (assoc component :store nil))

  DocstoreClient
  (put-item! [{:keys [store] :as component} table-name v]
    (let [table                                    (-> store deref table-name)
          [primary-key-name primary-key-type]     (-> table :schema :primary-key)
          [secondary-key-name secondary-key-type] (-> table :schema :secondary-key)
          primary-key-value                       (get v primary-key-name)
          secondary-key-value                     (get v secondary-key-name)]
      (when-not table
        (throw (ex-info "Can't do operations on non-existent table"
                        {:table table-name
                         :type  :non-existent-table})))
      (docstore-client.protocol/put-item! component
                 table-name
                 (assoc-if {primary-key-name   primary-key-value
                            secondary-key-name secondary-key-value}
                           secondary-key-name
                           secondary-key-value)
                 v)))

  (put-item! [{:keys [store]} table-name k v]
    (let  [table                                   (-> store deref table-name)
           [primary-key-name primary-key-type]     (-> table :schema :primary-key)
           [secondary-key-name secondary-key-type] (-> table :schema :secondary-key)
           primary-key-value                       (get k primary-key-name)
           secondary-key-value                     (or (get k secondary-key-name)
                                                       (get v secondary-key-name))]
      (when-not table
        (throw (ex-info "Can't do operations on non-existent table"
                        {:table table-name
                         :type  :non-existent-table})))
      
      (when-not primary-key-value
        (throw (ex-info "Missing primary key on item"
                        {:primary-key primary-key-name
                         :type        :missing-primary-key})))
      
      (when (and secondary-key-name (not secondary-key-value))
        (throw (ex-info "Missing secondary key on item"
                        {:secondary-key secondary-key-name
                         :type          :missing-secondary-key})))   
      (if-not secondary-key-name
        (swap! store (fn [tables]
                       (update-in tables [table-name :data]
                                  #(assoc % primary-key-value v))))
        (swap! store (fn [tables]
                       (update-in tables [table-name :data primary-key-value]
                                  #(assoc % secondary-key-value v)))))
      v))

  (get-item [{:keys [store]} table-name k {:keys [schema-resp]}]
    (let  [table                                   (-> store deref table-name)
           [primary-key-name primary-key-type]     (-> table :schema :primary-key)
           [secondary-key-name secondary-key-type] (-> table :schema :secondary-key)
           primary-key-value                       (get k primary-key-name)
           secondary-key-value                     (get k secondary-key-name)]
      (when-not table
        (throw (ex-info "Can't do operations on non-existent table"
                        {:table table-name
                         :type  :non-existent-table})))
      (when-not primary-key-value
        (throw (ex-info "Missing primary key"
                        {:primary-key primary-key-name
                         :type        :missing-primary-key})))
      (when (and secondary-key-name (not secondary-key-value))
        (throw (ex-info "Missing secondary key"
                        {:secondary-key secondary-key-name
                         :type          :missing-secondary-key})))
      (let [result
            (if-not secondary-key-name
              (get-in table [:data primary-key-value])
              (get-in table [:data primary-key-value secondary-key-value]))]
        (s/validate (s/maybe schema-resp) result))))

  (query [{:keys [store]} table-name primary-key-conditions]
    (let  [table                               (-> store deref table-name)
           [primary-key-name primary-key-type] (-> table :schema :primary-key)
           [secondary-key-name]                (-> table :schema :secondary-key)
           [cmp primary-key-value]             (get primary-key-conditions
                                                    primary-key-name)]
      (when-not table
        (throw (ex-info "Can't do operations on non-existent table"
                        {:table table-name
                         :type  :non-existent-table})))
      (when-not primary-key-value
        (throw (ex-info "Missing primary key"
                        {:primary-key primary-key-name
                         :type        :missing-primary-key})))   
      (if-not secondary-key-name
        (filterv (comp not nil?)
                 [(->> table
                       :data
                       (filter (fn [[k v]] ((cmp cmp->cmp-fn) k primary-key-value)))
                       first
                       second)])
        (->> table
             :data
             (filter (fn [[k v]] ((cmp cmp->cmp-fn) k primary-key-value)))
             first
             second
             vals
             vec)))))

(defn new-docstore-client []
  (map->InMemoryDocstoreClient {}))
