(ns common-clj.state-flow-helpers.config
  (:require [common-clj.components.config.protocol :as conf-pro]
            [state-flow.state :as state]))

(defn assoc-in! [ks v]
  (state/modify (fn [{{:keys [config]} :system :as state}]
                  (conf-pro/assoc-in! config ks v)
                  state)))
