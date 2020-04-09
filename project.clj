(defproject lucasmafra/common-clj "1.5.2"
  :description "Useful stuff for Clojure projects"
  :url "https://github.com/lucasmafra/common-clj" 
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [prismatic/schema "1.1.11"]
                 [prismatic/schema-generators "0.1.3"]
                 [metosin/schema-tools "0.12.0"]
                 [org.apache.kafka/kafka-clients "2.1.0"]
                 [org.clojure/test.check "0.10.0"]
                 [cheshire "5.9.0"]
                 [com.stuartsierra/component "0.4.0"]
                 [com.taoensso/encore "2.115.0"]
                 [com.taoensso/faraday "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.28"]
                 [org.slf4j/jcl-over-slf4j "1.7.28"]
                 [org.slf4j/log4j-over-slf4j "1.7.28"]
                 [clj-http "3.10.0"]
                 [clj-http-fake "1.0.3"]
                 [selmer "1.12.18"]]
  :resource-paths ["config", "resources"]
  :main ^:skip-aot common-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:aliases {"lint-fix" ["do" "nsorg" "--replace," "kibit" "--replace," "cljfmt" "fix"]
                                 "clj-kondo" ["run" "-m" "clj-kondo.main"]}
                       :dependencies [[nubank/matcher-combinators "1.0.0"]
                                      [nubank/state-flow "2.2.4"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [com.stuartsierra/component.repl "0.2.0"]
                                      [clj-kondo "2020.02.28-1"]]
                       :source-paths ["dev"]
                       :plugins [[lein-cljfmt "0.6.6"]
                                 [lein-nsorg "0.3.0"]
                                 [lein-kibit "0.1.8"]
                                 [clj-kondo "2020.02.28-1"]]}})
