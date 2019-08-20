(ns common-clj.components.config.in-memory-config-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.components.config.in-memory-config :as in-memory-config]
            [com.stuartsierra.component :as component]
            [common-clj.test-helpers :refer [schema-error?]])
  (:import clojure.lang.ExceptionInfo))

(def dummy-config
  {:app-name     :dummy
   :kafka-server "localhost:9090"})

(def invalid-config
  {:schrubles "schrubles"})

(s/with-fn-validation
  (fact "it takes the config map as an argument"
    (-> (in-memory-config/new-config dummy-config)
        component/start
        (config.protocol/get-config))
    => dummy-config)

  (fact "it throws when config does not conform to AppConfig schema"
    (-> (in-memory-config/new-config invalid-config)
        component/start)
    => (throws ExceptionInfo schema-error?)))
