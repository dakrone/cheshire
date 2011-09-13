(defproject cheshire "2.0.2-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.codehaus.jackson/jackson-core-asl "1.8.5"]
                 [org.codehaus.jackson/jackson-smile "1.8.5"]]
  :dev-dependencies [[lein-marginalia "0.6.0"]
                     [lein-multi "1.0.0"]]
  :multi-deps {"1.2.0" [[org.clojure/clojure "1.2.0"]
                        [org.codehaus.jackson/jackson-core-asl "1.8.5"]
                        [org.codehaus.jackson/jackson-smile "1.8.5"]]
               "1.3" [[org.clojure/clojure "1.3.0-RC0"]
                      [org.codehaus.jackson/jackson-core-asl "1.8.5"]
                      [org.codehaus.jackson/jackson-smile "1.8.5"]]})
