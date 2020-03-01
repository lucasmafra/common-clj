(ns common-clj.components.counter.in-memory-counter-test
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.counter.in-memory-counter :as in-memory-counter]
            [common-clj.components.counter.protocol :as counter.protocol]
            [midje.sweet :refer :all]))

#_(fact "it starts count at 0"
        (-> (in-memory-counter/new-counter)
            component/start
            counter.protocol/get-count)
        => 0)

#_(fact "it increments everytime inc! is called"
        (let [counter (component/start (in-memory-counter/new-counter))]
          (counter.protocol/inc! counter)
          (counter.protocol/get-count counter) => 1

          (counter.protocol/inc! counter)
          (counter.protocol/get-count counter) => 2

          (counter.protocol/inc! counter)
          (counter.protocol/get-count counter) => 3))
