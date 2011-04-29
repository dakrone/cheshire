(defproject cheshire "1.1.2"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.codehaus.jackson/jackson-core-asl "1.7.6"]
                 [org.codehaus.jackson/jackson-smile "1.7.6"]]
  :dev-dependencies [[marginalia "0.5.0"]
                     [lein-multi "1.0.0"]]
  :multi-deps {"1.2.0" [[org.clojure/clojure "1.2.0"]
                        [org.codehaus.jackson/jackson-core-asl "1.7.6"]
                        [org.codehaus.jackson/jackson-smile "1.7.6"]]
               "1.2.1" [[org.clojure/clojure "1.2.1"]
                        [org.codehaus.jackson/jackson-core-asl "1.7.6"]
                        [org.codehaus.jackson/jackson-smile "1.7.6"]]
               "1.3" [[org.clojure/clojure "1.3.0-alpha6"]
                      [org.codehaus.jackson/jackson-core-asl "1.7.6"]
                      [org.codehaus.jackson/jackson-smile "1.7.6"]]})
