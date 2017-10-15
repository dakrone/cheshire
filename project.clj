(defproject cheshire "5.8.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"
            :distribution :repo}
  :global-vars {*warn-on-reflection* false}
  :dependencies [[com.fasterxml.jackson.core/jackson-core "2.9.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.9.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor "2.9.0"]
                 [tigris "0.1.1"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/test.generative "0.1.4"]
                                  [org.clojure/tools.namespace "0.2.1"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0-beta2"]]}
             :benchmark {:test-paths ["benchmarks"]
                         :jvm-opts ^:replace ["-Xms1g" "-Xmx1g" "-server"]
                         :dependencies [[criterium "0.4.4"]
                                        [org.clojure/data.json "0.2.6"]
                                        [clj-json "0.5.3"]]}}
  :aliases {"all" ["with-profile" "dev,1.3:dev,1.4:dev,1.5:dev,1.7:dev,1.8:dev,1.9:dev"]
            "benchmark" ["with-profile" "dev,benchmark" "test"]
            "pretty-bench" ["with-profile" "dev,benchmark" "test" ":only"
                          "cheshire.test.benchmark/t-bench-pretty"]
            "core-bench" ["with-profile" "dev,benchmark" "test" ":only"
                          "cheshire.test.benchmark/t-bench-core"]}
  :test-selectors {:default  #(and (not (:benchmark %))
                                   (not (:generative %)))
                   :generative :generative
                   :all (constantly true)}
  :plugins [[codox "0.6.3"]]
  :java-source-paths ["src/java"]
  :jvm-opts ["-Xmx512M"
;;             "-XX:+PrintCompilation"
;;             "-XX:+UnlockDiagnosticVMOptions"
;;             "-XX:+PrintInlining"
             ]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
