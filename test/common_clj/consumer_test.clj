(ns common-clj.consumer-test
  (:require [midje.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defrecord KafkaConsumerImpl [name :- s/Str server :- s/Str]
  component/Lifecycle
  (start [component]
    (let [props (Properties.)]
      (.put props )
      (println "Started consumer...")))

  Consumer
  ( [component topic messag]))
