(defproject cheshire "3.0.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.codehaus.jackson/jackson-core-asl "1.9.5"]
                 [org.codehaus.jackson/jackson-smile "1.9.5"]]
  :profiles {:dev {:dependencies [[lein-marginalia "0.7.0"]
                                  [lein-multi "1.1.0"]
                                  [criterium "0.2.0"]]}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0-beta4"]]}}
  :aliases {"all" ["with-profile" "dev,1.2:dev:dev,1.4"]}
  :test-selectors {:default  #(not (:benchmark %))
                   :benchmark :benchmark
                   :all (constantly true)}
  :jvm-opts ["-Xmx512M"])
