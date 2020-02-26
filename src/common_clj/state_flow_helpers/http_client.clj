(ns common-clj.state-flow-helpers.http-client
  (:require [state-flow.state :as state]))

(defn mock!
  ([mock-calls]
   (mock! mock-calls :http-client))
  ([mock-calls component-key]
   (state/modify #(assoc-in % [:system component-key :mock-http-client-calls] mock-calls))))
