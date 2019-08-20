(ns common-clj.components.logger.protocol)

(defprotocol Logger
  (log! [component tag value])
  (get-logs [component tag]))
