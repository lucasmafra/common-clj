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
    (let [path-params-keys (or (-> endpoints endpoint :path-params-schema keys)
                          '())
          path-params-values (or (vals (filter (fn [[k]] (some #(= k %) path-params-keys)) options))
                                 '())
          response-path (conj path-params-values endpoint)
          response (-> mocked-responses
                       deref
                       (get-in response-path)
                       first)]
      (if response
        (do
          (swap! mocked-responses #(update-in % response-path rest))
          (:body response))
        (throw (ex-info "No response mocked"
                        {:type :http-client.error/no-response}))))))

(s/defn new-http-client [endpoints :- s-hc/Endpoints]
  (map->InMemoryHttpClient {:endpoints endpoints}))

(defn mock-response!
  ([http-client endpoint options]
   (mock-response! http-client endpoint {} options))
  ([{:keys [mocked-responses]} endpoint path-params {:keys [body]}]
   (let [path (or (-> path-params vals)
                  '())
         response-path (conj path endpoint)]
     (swap! mocked-responses
            #(update-in % response-path (comp vec conj) {:body body})))))
