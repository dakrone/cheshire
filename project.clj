(defproject cheshire "5.2.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"
            :distribution :repo}
  :warn-on-reflection false
  :dependencies [[com.fasterxml.jackson.core/jackson-core "2.2.3"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.2.3"]
                 [tigris "0.1.1"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                  [org.clojure/test.generative "0.1.4"]]}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :benchmark {:test-paths ["benchmarks"]
                         :dependencies [[criterium "0.4.2"]
                                        [org.clojure/data.json "0.2.3"]
                                        [clj-json "0.5.3"]]}}
  :aliases {"all" ["with-profile" "dev,1.3:dev,1.4:dev"]
            "benchmark" ["with-profile" "dev,benchmark" "test"]
            "core-bench" ["with-profile" "dev,benchmark" "test" ":only"
                          "cheshire.test.benchmark/t-bench-core"]}
  :test-selectors {:default  #(and (not (:benchmark %))
                                   (not (:generative %)))
                   :generative :generative
                   :all (constantly true)}
  :plugins [[codox "0.6.3"]]
  :jvm-opts ["-Xmx512M"
;;             "-XX:+PrintCompilation"
;;             "-XX:+UnlockDiagnosticVMOptions"
;;             "-XX:+PrintInlining"
             ])
