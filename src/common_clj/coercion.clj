(ns common-clj.coercion
  (:require [clojure.string :as str]
            [common-clj.misc :as misc]
            [common-clj.schema.core :as cs]
            [java-time :refer [instant local-date local-date-time]]
            [schema-tools.coerce :as stc]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [schema.utils :as utils])
  (:import schema.core.EnumSchema))

(def big-decimal-matcher (partial coerce/safe bigdec))
(def local-date-matcher (partial coerce/safe local-date))
(def local-date-time-matcher (partial coerce/safe local-date-time))
(def int-matcher (partial coerce/safe #(if (string? %)
                                         (Integer/parseInt %)
                                         (coerce/safe-long-cast %))))
(def pos-int-matcher (partial coerce/safe #(if (string? %)
                                             (Integer/parseInt %)
                                             (coerce/safe-long-cast %))))

(def epoch-millis-matcher (partial coerce/safe (comp instant #(Long/valueOf %))))

(def long-matcher (partial coerce/safe #(Long/valueOf %)))

(def enum-matcher (partial coerce/safe #(-> %
                                            name
                                            str/lower-case
                                            (misc/replace-char \_ \- #{}))))

(defn filter-schema-keys
  [m schema-keys extra-keys-walker]
  (reduce-kv (fn [m k _]
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

(defn make-matcher [match-schema coerce-fn]
  (fn [schema]
    (when (or (= match-schema schema) (and
                                       (instance? java.lang.Class match-schema)
                                       (instance? match-schema schema)))
      (coerce-fn))))

(defn json-matcher
  "Extends built-in schema json matcher to support more
   types like BigDecimals and LocalDate/LocalDateTime"
  [coercers {:keys [allow-extra-keys]}]
  (let [base-matchers (into
                       (reduce (fn [acc [k v]] (conj acc (make-matcher k v))) [] coercers)
                       [coerce/json-coercion-matcher])]
    (coerce/first-matcher (conj-if base-matchers
                                   allow-extra-keys
                                   map-filter-matcher))))

(def default-coercion-map
  {cs/LocalDate             local-date-matcher
   cs/LocalDateTime         local-date-time-matcher
   s/Int                    int-matcher
   cs/PosInt                pos-int-matcher
   java.math.BigDecimal     big-decimal-matcher
   cs/EpochMillis           epoch-millis-matcher
   cs/TimestampMicroseconds long-matcher
   EnumSchema               enum-matcher})

(defn coerce
  ([schema data]
   (coerce schema data default-coercion-map))
  ([schema data coercers]
   (coerce schema data coercers nil))
  ([schema data coercers options]
   (stc/coerce data schema (json-matcher coercers options))))
