(ns common-clj.components.logger.in-memory-logger-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.logger.in-memory-logger :as in-memory-logger]
            [common-clj.components.logger.protocol :as logger.protocol]
            [common-clj.test-helpers :refer [init!]]
            [midje.sweet :refer :all]))

(def system
  (component/system-map
   :logger (in-memory-logger/new-logger)))

(defn log!
  [tag value world]
  (let [logger (-> world :system :logger)]
    (logger.protocol/log! logger tag value)
    world))

#_(flow "log and retrieve values"
        (partial init! system)

        (partial log! :banana "apple")
        (partial log! :banana :lemon)
        (partial log! :grape {:strawberry 100})

        (fact "get logs by tag"
              (let [logger (-> *world* :system :logger)]
                (logger.protocol/get-logs logger :banana) => ["apple" :lemon]
                (logger.protocol/get-logs logger :grape) =>  [{:strawberry 100}])))
