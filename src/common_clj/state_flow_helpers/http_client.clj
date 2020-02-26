(ns common-clj.state-flow-helpers.http-client
  (:require [state-flow.state :as state]
            [com.stuartsierra.component :as component]
            [common-clj.http-client.interceptors.with-mock-calls :as i-mock]))

(defn mock! [mock-calls]
  (state/wrap-fn (fn [] (i-mock/mock-calls! mock-calls))))
