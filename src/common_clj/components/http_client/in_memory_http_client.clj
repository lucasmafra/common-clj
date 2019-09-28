(ns common-clj.components.http-client.in-memory-http-client
  (:require [com.stuartsierra.component :as component]
            [common-clj.components.http-client.protocol :as hc-pro]
            [common-clj.schemata.http-client :as s-hc]
            [schema.core :as s]))

(s/defrecord InMemoryHttpClient [endpoints :- s-hc/Endpoints]
  component/Lifecycle
  (start [component]
    (assoc component :mocked-responses (atom {})))

  (stop [component]
    component)

  hc-pro/HttpClient
  (request [component endpoint]
    (hc-pro/request component endpoint {}))
  (request [{:keys [mocked-responses]} endpoint options]
    (let [response (-> mocked-responses
                       deref
                       endpoint
                       first)]
      (if response
        (do
          (swap! mocked-responses #(update % endpoint rest))
          (:body response))
        (throw (ex-info "No response mocked"
                        {:type :http-client.error/no-response}))))))

(s/defn new-http-client [endpoints :- s-hc/Endpoints]
  (map->InMemoryHttpClient {:endpoints endpoints}))

(defn mock-response!
  [{:keys [mocked-responses]} endpoint {:keys [body]}]
  (swap! mocked-responses
         #(update % endpoint (comp vec conj) {:body body})))
