(ns aux.init
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [state-flow.cljtest :as state-flow.cljtest]
            [state-flow.core :as state-flow]
            [state-flow.state :as state]))

(defn init! [system]
  (state/modify #(assoc % :system (component/start system))))

(defn run!*
  [flow state]
  (s/with-fn-validation (state-flow/run! flow state)))

(defmacro defflow
  {:arglists '([name & flows]
               [name :pre-conditions pre-conditions & flows])}
  [name & forms]
  (let [[pre-conditions flows] (if (= :pre-conditions (first forms))
                                 [(second forms) (rest (rest forms))]
                                 [[] forms])]
    `(state-flow.cljtest/defflow ~name {:runner run!*}
       ~@pre-conditions
       ~@flows)))
