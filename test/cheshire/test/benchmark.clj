(ns cheshire.test.benchmark
  (:use [clojure.test])
  (:require [cheshire.core :as core]
            [cheshire.custom :as custom]
            [criterium.core :as bench]))

;; These tests just print out results, nothing else, they also
;; currently don't work with clojure 1.2 (but the regular tests do)

(def test-obj {"int" 3
               "boolean" true
               "LongObj" (Long/parseLong "2147483647")
               "double" 1.23
               "nil" nil
               "string" "string"
               "vec" [1 2 3]
               "map" {"a" "b"}
               "list" '("a" "b")
               "set" #{"a" "b"}
               "keyword" :foo})

(deftest ^{:benchmark true} t-bench-core
  (println "---------- Core Benchmarks ----------")
  (bench/with-progress-reporting
    (bench/quick-bench (core/decode (core/encode test-obj)) :verbose))
  (println "-------------------------------------"))

(deftest ^{:benchmark true} t-bench-custom
  (println "--------- Custom Benchmarks ---------")
  (bench/with-progress-reporting
    (bench/quick-bench (custom/decode (custom/encode test-obj)) :verbose))
  (println "-------------------------------------"))
