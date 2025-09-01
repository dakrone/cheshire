(defproject cheshire "6.1.0-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"
            :distribution :repo}
  :global-vars {*warn-on-reflection* false}
  :dependencies [[com.fasterxml.jackson.core/jackson-core "2.20.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.20.0"
                  :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor "2.20.0"
                  :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                 [tigris "0.1.2"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.12.2"]
                                  [org.clojure/test.generative "0.1.4"]
                                  [org.clojure/tools.namespace "0.3.1"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :1.11 {:dependencies [[org.clojure/clojure "1.11.4"]]}
             :benchmark {:test-paths ["benchmarks"]
                         :jvm-opts ^:replace ["-Xms1g" "-Xmx1g" "-server"]
                         :dependencies [[criterium "0.4.6"]
                                        [org.clojure/data.json "0.2.6"]
                                        [clj-json "0.5.3"]]}}
  :aliases {"all" ["with-profile" "dev,1.7:dev,1.8:dev,1.9:dev,1.10:dev"]
            "benchmark" ["with-profile" "dev,benchmark" "test"]
            "pretty-bench" ["with-profile" "dev,benchmark" "test" ":only"
                            "cheshire.test.benchmark/t-bench-pretty"]
            "core-bench" ["with-profile" "dev,benchmark" "test" ":only"
                          "cheshire.test.benchmark/t-bench-core"]
            "clj-kondo-deps" ["with-profile" "+test" "clj-kondo" "--skip-lint" "--copy-configs" "--dependencies" "--parallel" "--lint" "$classpath"]
            "clj-kondo-lint" ["do" ["clj-kondo-deps"] ["with-profile" "+test" "clj-kondo" "--parallel" "--lint" "src" "test" "project.clj"]]}
  :test-selectors {:default  #(and (not (:benchmark %))
                                   (not (:generative %)))
                   :generative :generative
                   :all (constantly true)}
  :plugins [[com.github.liquidz/antq "2.11.1276"]
            [jonase/eastwood "1.4.3"]
            [com.github.clj-kondo/lein-clj-kondo "2025.04.07"]]
  :java-source-paths ["src/java"]
  :jvm-opts ["-Xmx1024M"
;;             "-XX:+PrintCompilation"
;;             "-XX:+UnlockDiagnosticVMOptions"
;;             "-XX:+PrintInlining"
             ]
  :eastwood {:namespaces [:source-paths]
             :linters [:deprecations]}
  :javac-options
  ~(let [version (System/getProperty "java.version")
        ;; Parse major version from strings like "1.8.0_292" or "11.0.11"
         major (-> (re-find #"^(1\.)?(\d+)" version)
                   (last)
                   (Integer/parseInt))
         target-opts (case major
                       8 ["-source" "1.8" "-target" "1.8"]
                       (if (>= major 9)
                         ["--release" "8"]
                         (throw (ex-info "javac needs a min of JDK 8" {}))))]
     (into target-opts ["-Xlint:-options" "-Xlint:deprecation" "-Werror"])))
