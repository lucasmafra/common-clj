(ns common-clj.components.consumer.protocol)

(defprotocol Consumer
  (consume! [component topic message]))
