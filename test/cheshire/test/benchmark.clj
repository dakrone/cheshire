(ns cheshire.test.benchmark
  (:use [clojure.test])
  (:require [cheshire.core :as core]
            [cheshire.custom :as custom]
            [criterium.core :as bench]))

;; These tests just print out results, nothing else

(def test-obj {"int" 3 "long" (long -2147483647) "boolean" true
               "LongObj" (Long/parseLong "2147483647") "double" 1.23
               "nil" nil "string" "string" "vec" [1 2 3] "map" {"a" "b"}
               "list" (list "a" "b")})

(deftest ^{:benchmark true} t-bench-core
  (println "---------- Core Benchmarks ----------")
  (bench/with-progress-reporting
    (bench/bench (core/decode (core/encode test-obj)) :verbose))
  (println "-------------------------------------"))

(deftest ^{:benchmark true} t-bench-custom
  (println "--------- Custom Benchmarks ---------")
  (bench/with-progress-reporting
    (bench/bench (custom/decode (custom/encode test-obj)) :verbose))
  (println "-------------------------------------"))
