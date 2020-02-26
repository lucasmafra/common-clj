(ns common-clj.components.config.protocol)

(defprotocol Config
  (get-config [component])
  (get-env [component])
  (assoc-in! [component ks v]))
