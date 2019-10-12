(ns common-clj.uuid)

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn random [] (java.util.UUID/randomUUID))
