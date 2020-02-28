(ns common-clj.misc
  (:require [schema.core :as s]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(def string-or-keyword (s/if keyword? s/Keyword s/Str))

(s/defn ^:private replace-char :- s/Keyword
  ;; Replaces the from character with the to character in s, which can be a String or a Keyword
  ;; Does nothing if s is a keyword that is in the exception set
  [s :- string-or-keyword, from :- Character, to :- Character, exceptions :- #{s/Keyword}]
  (if (contains? exceptions s) s (keyword (str/replace (name s) from to))))

(s/defn ^:private replace-char-gen :- (s/pred fn?)
  ;; Will replace dashes with underscores or underscores with dashes for the keywords in a map
  ;; Ignores String values in a map (both keys and values)
  ([from :- Character, to :- Character] (replace-char-gen from to #{}))
  ([from :- Character, to :- Character, exceptions :- #{s/Keyword}]
    #(if (keyword? %) (replace-char % from to exceptions) %)))

(defn underscore->dash
  "Convert hash-map underscored keywords to dash.
  Example:
    => (underscore->dash {:foo_bar {:bar_foo 1}})
    {:foo-bar {:bar-foo 1}}
  "
  [m]
  (walk/postwalk (replace-char-gen \_ \-) m))

(defn dash->underscore
  "Inverse of underscore->dash
  Example:
    => (dash->underscore {:foo-bar {:bar-foo 1}})
    {:foo_bar {:bar_foo 1}}
  "
  [m]
  (walk/postwalk (replace-char-gen \- \_) m))

(defn camelcase->dash [m]
  (walk/postwalk
   #(if (keyword? %)
      (-> (str/replace (name %) #"([A-Z])" "-$1")
          (.toLowerCase)
          (str/replace #"^-" "")
          keyword)
      %)
   m))

(defmacro for-map
  "Like 'for' for building maps. Same bindings except the body should have a
  key-expression and value-expression. If a key is repeated, the last
  value (according to \"for\" semantics) will be retained.
  (= (for-map [i (range 2) j (range 2)] [i j] (even? (+ i j)))
     {[0 0] true, [0 1] false, [1 0] false, [1 1] true})
  An optional symbol can be passed as a first argument, which will be
  bound to the transient map containing the entries produced so far."
  ([seq-exprs key-expr val-expr]
     `(for-map ~(gensym "m") ~seq-exprs ~key-expr ~val-expr))
  ([m-sym seq-exprs key-expr val-expr]
     `(let [m-atom# (atom (transient {}))]
        (doseq ~seq-exprs
          (let [~m-sym @m-atom#]
            (reset! m-atom# (assoc! ~m-sym ~key-expr ~val-expr))))
        (persistent! @m-atom#))))

(defn map-vals
  "Build map k -> (f v) for [k v] in map, preserving the initial type"
  [f m]
  (cond
   (sorted? m)
   (reduce-kv (fn [out-m k v] (assoc out-m k (f v))) (sorted-map) m)
   (map? m)
   (persistent! (reduce-kv (fn [out-m k v] (assoc! out-m k (f v))) (transient {}) m))
   :else
   (for-map [[k v] m] k (f v))))
