(ns common-clj.http-client.interceptors.url-builder-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-client.interceptors.url-builder :as nut]
            [io.pedestal.interceptor.chain :as chain]))

(def config
  {:app-name    :app
   :known-hosts {:my-service "http://my-service.com"}})

(def context
  {:endpoints
   {:service/hello
    {:host "http://service.com"}}

   :endpoint      :service/hello
   :path-replaced "/api/hello"

   :components
   {:config (component/start
             (imc/new-config config))}})

(deftest url-builder
  (testing "builds url and assocs to context"
    (is (= "http://service.com/api/hello"
           (get-in (chain/execute context [nut/url-builder])
                   [:url]))))

  (testing "when host is a variable, gets the value from config"
    (let [context (-> context
                      (assoc-in [:endpoints :service/hello :host] "{{my-service}}"))]
      (is (= "http://my-service.com/api/hello"
             (get-in (chain/execute context [nut/url-builder])
                     [:url]))))))
