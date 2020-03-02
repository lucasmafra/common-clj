(ns common-clj.state-flow-helpers.config
  (:require [common-clj.config.protocol :as config-pro]
            [state-flow.state :as state]))

(defn assoc-in! [ks v]
  (state/modify (fn [{{:keys [config]} :system :as state}]
                  (config-pro/assoc-in! config ks v)
                  state)))
