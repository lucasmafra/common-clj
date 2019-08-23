(ns common-clj.uuid)

(defn uuid [] (.toString (java.util.UUID/randomUUID)))
