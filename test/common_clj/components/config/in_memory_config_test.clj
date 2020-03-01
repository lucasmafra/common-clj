(ns common-clj.components.config.in-memory-config-test)

(def dummy-config
  {:app-name     :dummy})

(def invalid-config
  {:schrubles "schrubles"})

#_(s/with-fn-validation
    (fact "it takes the config map as an argument"
          (-> (in-memory-config/new-config dummy-config)
              component/start
              (config.protocol/get-config))
          => dummy-config)

    (fact "it throws when config does not conform to AppConfig schema"
          (-> (in-memory-config/new-config invalid-config)
              component/start)
          => (throws ExceptionInfo schema-error?))

    (facts "get-env"
           (fact "defaults to prod"
                 (-> (in-memory-config/new-config dummy-config)
                     component/start
                     config.protocol/get-env)
                 => :prod)

           (fact "can be overwritten"
                 (-> (in-memory-config/new-config dummy-config :test)
                     component/start
                     config.protocol/get-env)
                 => :test)))
