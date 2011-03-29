(defproject cheshire "1.1.0"
  :description "JSON and JSON SMILE encoding, fast."
  :url "https://github.com/dakrone/cheshire"
  :source-path "src/clj"
  :java-source-path "src/jvm"
  :javac-fork "true"
  :dependencies
  [[org.clojure/clojure "1.2.1"]
   [org.codehaus.jackson/jackson-core-asl "1.7.4"]
   [org.codehaus.jackson/jackson-smile "1.7.4"]]
  :dev-dependencies [[marginalia "0.5.0"]])
