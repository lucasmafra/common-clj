(ns common-clj.clojure-test-helpers.core
  (:require clojure.test))

(defmacro deftest [name & body]
  (let [tests (mapv (fn [t] `(schema.core/with-fn-validation ~t)) body)]
    `(clojure.test/deftest ~name
       ~@tests)))
