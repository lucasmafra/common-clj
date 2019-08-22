(ns common-clj.components.producer.protocol)

(defprotocol Producer
  (produce! [component topic message]))
