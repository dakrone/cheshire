(defproject cheshire "1.0.4"
  :description "JSON and JSON SMILE encoding"
  :url "https://github.com/dakrone/cheshire"
  :source-path "src/clj"
  :java-source-path "src/jvm"
  :javac-fork "true"
  :dependencies
    [[org.clojure/clojure "1.2.1"]
     [org.clojure/clojure-contrib "1.2.0"]
     [org.codehaus.jackson/jackson-core-asl "1.7.4"]
     [org.codehaus.jackson/jackson-smile "1.7.4"]]
  :dev-dependencies
    [[org.clojars.mmcgrana/lein-javac "1.2.1"]])
