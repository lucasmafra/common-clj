(defproject lucasmafra/common-clj "0.40.0"
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
                 [io.pedestal/pedestal.jetty "0.5.7"]]
  :main ^:skip-aot common-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:aliases {"lint-fix" ["do" "nsorg" "--replace," "kibit" "--replace"]}
                       :dependencies [[midje "1.9.8"]
                                      [nubank/matcher-combinators "1.0.0"]
                                      [nubank/selvage "1.0.0-BETA"]]
                       :plugins [[lein-midje "3.2.1"]
                                 [lein-nsorg "0.3.0"]
                                 [lein-kibit "0.1.7"]]
                       :source-paths ["src"]
                       :main user}})
