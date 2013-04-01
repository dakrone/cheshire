(ns cheshire.test.benchmark
  (:use [clojure.test])
  (:require [cheshire.core :as core]
            [cheshire.generate :as custom]
            [clojure.data.json :as cj]
            [clj-json.core :as clj-json]
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

(deftest t-bench-clj-json
  (println "-------- clj-json Benchmarks --------")
  (bench/with-progress-reporting
    (bench/quick-bench (clj-json/parse-string
                        (clj-json/generate-string test-obj)) :verbose))
  (println "-------------------------------------"))

(deftest t-bench-clojure-json
  (println "-------- Data.json Benchmarks -------")
  (bench/with-progress-reporting
    (bench/quick-bench (cj/read-str (cj/write-str test-obj)) :verbose))
  (println "-------------------------------------"))

(deftest t-bench-core
  (println "---------- Core Benchmarks ----------")
  (bench/with-progress-reporting
    (bench/quick-bench (core/decode (core/encode test-obj)) :verbose))
  (println "-------------------------------------"))

(deftest t-bench-custom
  (println "--------- Custom Benchmarks ---------")
  (custom/add-encoder java.net.URL custom/encode-str)
  (is (= "\"http://foo.com\"" (core/encode (java.net.URL. "http://foo.com"))))
  (let [custom-obj (assoc test-obj "url" (java.net.URL. "http://foo.com"))]
    (println "[+] Custom, all custom fields:")
    (bench/with-progress-reporting
      (bench/quick-bench (core/decode (core/encode custom-obj)) :verbose)))
  (println "-------------------------------------"))

(deftest t-bench-custom-kw-coercion
  (println "---- Custom keyword-fn Benchmarks ---")
  (let [t (core/encode test-obj)]
    (println "[+] (fn [k] (keyword k)) decode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/decode t (fn [k] (keyword k)))))
    (println "[+] basic 'true' keyword-fn decode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/decode t true)))
    (println "[+] no keyword-fn decode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/decode t)))
    (println "[+] (fn [k] (keyword k)) encode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/encode test-obj {:key-fn (fn [k] (name k))})))
    (println "[+] no keyword-fn encode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/encode test-obj))))
  (println "-------------------------------------"))
