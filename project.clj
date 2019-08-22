(defproject lucasmafra/common-clj "0.2.0"
  :description "Useful stuff for Clojure projects"
  :url "https://github.com/lucasmafra/common-clj" 
 :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [prismatic/schema "1.1.11"]
                 [org.apache.kafka/kafka-clients "2.1.0"]
                 [cheshire "5.9.0"]
                 [com.stuartsierra/component "0.4.0"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [org.slf4j/slf4j-simple "1.7.25"]]
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
