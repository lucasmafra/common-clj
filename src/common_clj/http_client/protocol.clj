(ns common-clj.http-client.protocol)

(defprotocol HttpClient
  (request
    [component endpoint]
    [component endpoint options]))
