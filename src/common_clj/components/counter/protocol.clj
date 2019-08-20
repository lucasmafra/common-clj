(ns common-clj.components.counter.protocol)

(defprotocol Counter
  (inc! [component])
  (get-count [component]))
