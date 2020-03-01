(ns common-clj.state-flow-helpers.http-client
  (:require [common-clj.http-client.interceptors.with-mock-calls :as i-mock]
            [state-flow.state :as state]))

(defn mock! [mock-calls]
  (state/wrap-fn (fn [] (i-mock/mock-calls! mock-calls))))
