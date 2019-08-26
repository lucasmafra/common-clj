(ns common-clj.components.http-server.protocol)

(defprotocol HttpServer
  (create-server [component]))

