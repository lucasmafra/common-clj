(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [reset set-init stop]]
            [common-clj.components.config.in-memory-config :as imc]
            [common-clj.components.http-server.http-server :as hs]
            [common-clj.schema :as cs]
            [schema.core :as s]
            [common-clj.coercion :as coercion]))

(def routes
  {:route/hello
   {:path            "/hello"
    :method          :get
    :handler         (constantly (hs/ok {:message "Ok"}))
    :response-schema s/Any}

   :route/demo-path-params
   {:path               "/demo/path-params/:x"
    :method             :get
    :handler            (fn [{{:keys [x]} :path-params} _] (hs/ok {:message (str "x: " x)}))
    :path-params-schema {:x cs/PosInt}
    :response-schema    s/Any}

   :route/demo-query-params
   {:path                "/demo/query"
    :method              :get
    :handler             (fn [{{:keys [age name]} :query-params} _]
                           (hs/ok {:message (str "Age: " age " Name: " name)}))
    :query-params-schema {:age s/Int :name s/Str}
    :response-schema     s/Any}

   :route/demo-post
   {:path            "/demo/post"
    :method          :post
    :handler         (fn [{{:keys [age name]} :body} _]
                       (hs/ok {:new-name (str name "Bob")}))
    :request-schema  {:date cs/LocalDate  :age cs/PosInt :obj {:age cs/LocalDateTime}}
    :response-schema {:new-name s/Str}}})

(def config
  {:app-name :hello-world
   :http-port 9000})

(def coercers
  (merge
   hs/default-coercers
   {cs/LocalDate coercion/local-date-matcher}))

(def overrides
  {:override-coercers coercers})

(def system
  (component/system-map
   :config (imc/new-config config)
   :http-server (component/using
                 (hs/new-http-server routes overrides)
                 [:config])))

(set-init (constantly system))
