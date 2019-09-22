(ns common-clj.components.http-client.protocol)

(defprotocol HttpClient
  (request [component endpoint options]))
