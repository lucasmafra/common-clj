(ns common-clj.components.config.protocol)

(defprotocol Config
  (get-config [component]))