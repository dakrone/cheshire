(defproject cheshire "2.1.1-SNAPSHOT"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.codehaus.jackson/jackson-core-asl "1.9.4"]
                 [org.codehaus.jackson/jackson-smile "1.9.4"]]
  :dev-dependencies [[lein-marginalia "0.6.0"]
                     [lein-multi "1.0.0"]]
  :multi-deps {"1.2.1" [[org.clojure/clojure "1.2.1"]
                        [org.codehaus.jackson/jackson-core-asl "1.9.4"]
                        [org.codehaus.jackson/jackson-smile "1.9.4"]]
               "1.4.0" [[org.clojure/clojure "1.4.0-alpha3"]
                        [org.codehaus.jackson/jackson-core-asl "1.9.4"]
                        [org.codehaus.jackson/jackson-smile "1.9.4"]]})
