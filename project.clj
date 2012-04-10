(defproject cheshire "3.1.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.0.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.0.0"]]
  :profiles {:dev {:dependencies [[lein-marginalia "0.7.0"]
                                  [lein-multi "1.1.0"]
                                  [criterium "0.2.0"]
                                  [org.clojure/test.generative "0.1.4"]]}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0-beta7"]]}}
  :aliases {"all" ["with-profile" "dev,1.2:dev:dev,1.4"]}
  :test-selectors {:default  #(not (:benchmark %))
                   :benchmark :benchmark
                   :generative :generative
                   :all (constantly true)}
  :jvm-opts ["-Xmx512M"])
