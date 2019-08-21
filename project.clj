(defproject common-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [prismatic/schema "1.1.11"]
                 [org.apache.kafka/kafka-clients "2.1.0"]
                 [ymilky/franzy-json "0.0.1"]
                 [com.fasterxml.jackson.core/jackson-core "2.9.9"]
                 [com.stuartsierra/component "0.4.0"]]
  :main ^:skip-aot common-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.9.8"]
                                      [nubank/matcher-combinators "1.0.0"]
                                      [nubank/selvage "1.0.0-BETA"]]
                       :source-paths ["src/dev"]
                       :main user}})
