(ns common-clj.coercion
  (:require [java-time :refer [local-date local-date-time]]
            [schema-tools.coerce :as stc]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [schema.utils :as utils]))

(defn big-decimal-matcher [schema]
  (when (= java.math.BigDecimal schema)
    (coerce/safe bigdec)))

(defn local-date-matcher [schema]
  (when (= java.time.LocalDate schema)
    (coerce/safe local-date)))

(defn local-date-time-matcher [schema]
  (when (= java.time.LocalDateTime schema)
    (coerce/safe local-date-time)))

(defn filter-schema-keys
  [m schema-keys extra-keys-walker]
  (reduce-kv (fn [m k v]
               (if (or (contains? schema-keys k)
                       (and extra-keys-walker
                            (not (utils/error? (extra-keys-walker k)))))
                 m
                 (dissoc m k)))
             m
             m))

(defn map-filter-matcher
  [s]
  (when (or (instance? clojure.lang.PersistentArrayMap s)
            (instance? clojure.lang.PersistentHashMap s))
    (let [extra-keys-schema (#'s/find-extra-keys-schema s)
          extra-keys-walker (when extra-keys-schema (s/checker extra-keys-schema))
          explicit-keys (some->> (dissoc s extra-keys-schema)
                                 keys
                                 (mapv s/explicit-schema-key)
                                 (into #{}))]
      (when (or extra-keys-walker (seq explicit-keys))
        (fn [x]
          (if (map? x)
            (filter-schema-keys x explicit-keys extra-keys-walker)
            x))))))

(defn conj-if
  [coll condition val]
  (if condition
    (conj coll val)
    coll))

(defn json-matcher
  "Extends built-in schema json matcher to support more
   types like BigDecimals and LocalDate/LocalDateTime"
  [{:keys [allow-extra-keys]}]
  (let [base-matchers [local-date-matcher
                       local-date-time-matcher
                       big-decimal-matcher
                       coerce/json-coercion-matcher]]
    (coerce/first-matcher (conj-if base-matchers
                                   allow-extra-keys
                                   map-filter-matcher))))

(defn coerce
  ([schema data]
   (coerce schema data nil))
  ([schema data options]
   (stc/coerce data schema (json-matcher options))))

