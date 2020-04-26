(ns common-clj.kafka.consumer.interceptors.helpers)

(defn parse-overrides [{:keys [overrides]} k default]
  (merge default (k overrides)))

(defn match-topic? [record]
  (fn [[_ {:keys [topic]}]] (= topic (.topic record))))
