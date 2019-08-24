(ns common-clj.components.docstore-client.in-memory-docstore-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.docstore-client.protocol :refer [DocstoreClient]]
            [schema.core :as s]))

(def cmp->cmp-fn
  "Maps comparison-operators symbols to comparison functions
   Symbols yet to support:
    #{:le :lt :ge :gt :begins-with :between :ne
      :not-null :null :contains :not-contains :in}"
  {:eq =})

(s/defrecord InMemoryDocstoreClient []
  component/Lifecycle
  (start [component]
    (assoc component :store (atom {})))

  (stop [component]
    (assoc component :store nil))

  DocstoreClient
  (ensure-table! [{:keys [store]} table-name primary-key-schema]
    (let [table (-> store deref table-name)]
      (when-not table
        (swap! store
               (fn [tables]
                 (assoc tables table-name {:schema {:primary-key primary-key-schema}
                                           :data   {}}))))))

  (ensure-table! [{:keys [store]} table-name primary-key-schema
                  secondary-key-schema]
    (let [table (-> store deref table-name)]
      (when-not table
        (swap! store
               (fn [tables]
                 (assoc tables table-name {:schema
                                           {:primary-key   primary-key-schema
                                            :secondary-key secondary-key-schema}
                                           :data {}}))))))

  (delete-table! [{:keys [store]} table-name]
    (let [table (-> store deref table-name)]
      (when-not table
        (throw (ex-info "Can't do operations on non-existent table"
                        {:table table-name
                         :type  :non-existent-table})))
      (swap! store (fn [tables] (dissoc tables table-name)))))

  (put-item! [{:keys [store]} table-name item]
    (let  [table                                   (-> store deref table-name)
           [primary-key-name primary-key-type]     (-> table :schema :primary-key)
           [secondary-key-name secondary-key-type] (-> table :schema :secondary-key)
           primary-key-value                       (get item primary-key-name)
           secondary-key-value                     (get item secondary-key-name)]
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
                                  #(assoc % primary-key-value item))))
        (swap! store (fn [tables]
                       (update-in tables [table-name :data primary-key-value]
                                  #(assoc % secondary-key-value item)))))))

  (get-item [{:keys [store]} table-name item-key]
    (let  [table                                   (-> store deref table-name)
           [primary-key-name primary-key-type]     (-> table :schema :primary-key)
           [secondary-key-name secondary-key-type] (-> table :schema :secondary-key)
           primary-key-value                       (get item-key primary-key-name)
           secondary-key-value                     (get item-key secondary-key-name)]
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
      (if-not secondary-key-name
        (get-in table [:data primary-key-value])
        (get-in table [:data primary-key-value secondary-key-value]))))

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
        (filterv (comp not nil?) [(->> table :data (filter (fn [[k v]] ((cmp cmp->cmp-fn) k primary-key-value))) first second)])
        (->> table
             :data
             (filter (fn [[k v]] ((cmp cmp->cmp-fn) k primary-key-value)))
             first
             second
             vals
             vec)))))

(defn new-docstore-client []
  (map->InMemoryDocstoreClient {}))

