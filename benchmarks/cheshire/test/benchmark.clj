(ns cheshire.test.benchmark
  (:use [clojure.test])
  (:require [cheshire.core :as core]
            [cheshire.custom :as old]
            [cheshire.generate :as custom]
            [clojure.data.json :as cj]
            [clojure.java.io :refer [file input-stream resource reader]]
            [clj-json.core :as clj-json]
            [criterium.core :as bench])
  (:import (java.util.zip GZIPInputStream)))

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

(def test-pretty-opts
  {:indentation 4
   :indent-arrays? true
   :object-field-value-separator ": "})

(defn big-test-reader [f]
  (-> f
      file
      input-stream
      (GZIPInputStream.)
      reader))

(def big-test-obj
  (-> (big-test-reader "test/all_month.geojson.gz")
      slurp
      core/decode))

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
  (println "........encode/decode full object")
  (bench/with-progress-reporting
    (bench/bench (core/decode (core/encode test-obj)) :verbose))
  (println "........decode single nested property")
  (bench/with-progress-reporting
    (bench/bench (take 5 (core/parse-stream
                          (big-test-reader "test/all_month.geojson.gz")
                          nil nil
                          [#{"features"} #{"properties"} #{"url"}] true)) :verbose))
  (println "-------------------------------------"))

(deftest t-bench-pretty
  (let [pretty-printer (core/create-pretty-printer test-pretty-opts)]
    (println "------- PrettyPrint Benchmarks ------")
    (println "........default pretty printer")
    (bench/bench (core/encode test-obj {:pretty true}))
    (println "........custom pretty printer")
    (bench/bench (core/encode test-obj {:pretty pretty-printer}))))

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
    (println "[+] (fn [k] (name k)) encode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/encode test-obj {:key-fn (fn [k] (name k))})))
    (println "[+] no keyword-fn encode")
    (bench/with-progress-reporting
      (bench/quick-bench (core/encode test-obj))))
  (println "-------------------------------------"))

(deftest t-large-array
  (println "-------- Large array parsing --------")
  (let [test-array-json (core/encode (range 1024))]
    (bench/with-progress-reporting
      (bench/bench (pr-str (core/decode test-array-json)))))
  (println "-------------------------------------"))

(deftest t-large-geojson-object
  (println "------- Large GeoJSON parsing -------")
  (println "[+] large geojson custom encode")
  (bench/with-progress-reporting
    (bench/quick-bench (old/encode big-test-obj)))
  (println "[+] large geojson encode")
  (bench/with-progress-reporting
    (bench/quick-bench (core/encode big-test-obj)))
  (println "[+] large geojson decode")
  (let [s (core/encode big-test-obj)]
    (bench/with-progress-reporting
      (bench/quick-bench (core/decode s))))
  (println "-------------------------------------"))
