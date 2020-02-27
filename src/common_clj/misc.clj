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
