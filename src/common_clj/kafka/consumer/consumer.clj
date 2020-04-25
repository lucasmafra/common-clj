(ns common-clj.kafka.consumer.consumer
  (:require [com.stuartsierra.component :as component]
            [common-clj.config.protocol :as conf-pro]
            [common-clj.kafka.consumer.interceptors.consumer-loop :as i-loop]
            [common-clj.kafka.consumer.interceptors.handler :as i-handler]
            [common-clj.kafka.consumer.interceptors.kafka-client :as i-kafka-client]
            [common-clj.kafka.consumer.interceptors.mock-consumer-loop :as i-mock-loop]
            [common-clj.kafka.consumer.interceptors.mock-kafka-client :as i-mock-kafka-client]
            [common-clj.kafka.consumer.interceptors.subscriber :as i-subscriber]
            [io.pedestal.interceptor.chain :as chain]
            [schema.core :as s]))

(def default-interceptors
  [i-handler/handler])

(def build-interceptors (constantly default-interceptors))

(def test-interceptors
  [i-mock-kafka-client/mock-kafka-client i-mock-loop/mock-consumer-loop])

(defn build-start-interceptors [env]  
  (cond->> [i-kafka-client/kafka-client i-subscriber/subscriber i-loop/consumer-loop]
    (= env :test) (concat test-interceptors)))

(s/defrecord KafkaConsumer [topics]
  component/Lifecycle
  (start [{:keys [config producer] :as component}]
    (let [env                    (conf-pro/get-env config)
          config                 (conf-pro/get-config config)
          start-interceptors     (build-start-interceptors env)
          consume-interceptors   (build-interceptors env)
          context                {:config               config
                                  :topics               topics
                                  :consume-interceptors consume-interceptors
                                  :components           component}
          {:keys [kafka-client]} (chain/execute context start-interceptors)]
      (assoc component :kafka-client kafka-client)))

  (stop [{:keys [kafka-client] :as component}]
    (.wakeup kafka-client)
    (dissoc component :kafka-client)))

(defn new-consumer [topics]
  (map->KafkaConsumer {:topics topics}))
