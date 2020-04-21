(ns common-clj.kafka.producer.protocol)

(defprotocol Producer
  (produce! [component topic message]))
