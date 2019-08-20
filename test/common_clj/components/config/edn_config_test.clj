(ns common-clj.components.config.edn-config-test
  (:require [midje.sweet :refer :all]
            [common-clj.components.config.edn-config :as edn-config]
            [common-clj.components.config.protocol :as config.protocol]
            [common-clj.test-helpers :refer [schema-error?]]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io]))

(def app-config (io/resource "fixtures/app.edn"))
(def invalid-app-config (io/resource "fixtures/invalid_app.edn"))

(s/with-fn-validation
  (fact "it reads from resources/app.edn"
    (-> (edn-config/new-config)
        component/start
        config.protocol/get-config)
    => {:app-name     :common-clj
        :kafka-server "localhost:9092"}
    (provided
     (io/resource "app.edn") => app-config))
  
  (fact "it throws when config does not conform to AppConfig schema"
    (-> (edn-config/new-config)
        component/start)
    => (throws clojure.lang.ExceptionInfo schema-error?)
    (provided
     (io/resource "app.edn") => invalid-app-config))

  (fact "it throws when it doesn't find the config file"
    (-> (edn-config/new-config)
        component/start)
    => (throws Exception (str "Error parsing edn file. "
                              "Make sure you have your app config at 'resources/app.edn'"))
    (provided
     (io/resource "app.edn") => nil)))
