(defproject cheshire "4.0.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.0.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.0.0"]]
  :profiles {:dev {:dependencies [[criterium "0.2.1"]
                                  [org.clojure/test.generative "0.1.4"]
                                  [org.clojure/data.json "0.1.2"]
                                  [clj-json "0.5.0"]
                                  [expectations "1.4.3"]]}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}}
  :aliases {"all" ["with-profile" "dev,1.2:dev,1.3:dev"]}
  :plugins [[lein-expectations "0.0.5"]]
  :test-selectors {:default  #(and (not (:benchmark %))
                                   (not (:generative %)))
                   :benchmark :benchmark
                   :generative :generative
                   :all (constantly true)}
  :jvm-opts ["-Xmx512M"])
