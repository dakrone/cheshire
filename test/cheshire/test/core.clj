(ns cheshire.test.core
  (:use [clojure.test]
        [clojure.java.io :only [reader]])
  (:require [cheshire.core :as json])
  (:import (java.io StringReader StringWriter
                    BufferedReader BufferedWriter)
           (java.util Date UUID)))

(def test-obj {"int" 3 "long" (long -2147483647) "boolean" true
               "LongObj" (Long/parseLong "2147483647") "double" 1.23
               "nil" nil "string" "string" "vec" [1 2 3] "map" {"a" "b"}
               "list" (list "a" "b")})

(deftest t-bigint
  (let [n 9223372036854775808]
    (is (= n (:num (json/decode (json/encode {:num n}) true))))))

(deftest test-string-round-trip
  (is (= test-obj (json/parse-string (json/generate-string test-obj)))))

(deftest test-generate-accepts-float
  (is (= "3.14" (json/generate-string 3.14))))

(deftest test-keyword-encode
  (is (= {"key" "val"}
         (json/parse-string (json/generate-string {:key "val"})))))

(deftest test-generate-set
  (is (= {"set" ["a" "b"]}
         (json/parse-string (json/generate-string {"set" #{"a" "b"}})))))

(deftest test-key-coercion
  (is (= {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"}
         (json/parse-string
          (json/generate-string
           {:foo "bar" 1 "bat" (long 2) "bang" (bigint 3) "biz"})))))

(deftest test-keywords
  (is (= {:foo "bar" :bat 1}
         (json/parse-string
          (json/generate-string {:foo "bar" :bat 1})
          true))))

(deftest test-symbols
  (is (= {"foo" "clojure.core/map"}
         (json/parse-string
          (json/generate-string {"foo" 'clojure.core/map})))))

(deftest test-parsed-seq
  (let [br (BufferedReader. (StringReader. "1\n2\n3\n"))]
    (is (= (list 1 2 3) (json/parsed-seq br)))))

(deftest test-smile-round-trip
  (is (= test-obj (json/parse-smile (json/generate-smile test-obj)))))

(deftest test-aliases
  (is (= {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"}
         (json/decode
          (json/encode
           {:foo "bar" 1 "bat" (long 2) "bang" (bigint 3) "biz"})))))

(deftest test-date
  (is (= {"foo" "1970-01-01T00:00:00Z"}
         (json/decode
          (json/encode
           {:foo (Date. (long 0))}))))
  (is (= {"foo" "1970-01-01"}
         (json/decode
          (json/encode
           {:foo (Date. (long 0))} "yyyy-MM-dd")))
      "encode with given date format"))

(deftest test-uuid
  (let [id (UUID/randomUUID)
        id-str (str id)]
    (is (= {"foo" id-str}
           (json/decode
            (json/encode
             {:foo id}))))))

(deftest test-streams
  (is (= {"foo" "bar"}
         (json/parse-stream
          (BufferedReader. (StringReader. "{\"foo\":\"bar\"}\n")))))
  (let [sw (StringWriter.)
        bw (BufferedWriter. sw)]
    (json/generate-stream {"foo" "bar"} bw)
    (is (= "{\"foo\":\"bar\"}" (.toString sw))))
  (is (= {(keyword "foo baz") "bar"}
         (with-open [rdr (StringReader. "{\"foo baz\":\"bar\"}\n")]
           (json/parse-stream rdr true)))))

(deftest test-jsondotorg-pass1
  (let [string (slurp "test/pass1.json")
        decoded-json (json/decode string)
        encoded-json (json/encode decoded-json)
        re-decoded-json (json/decode encoded-json)]
    (is (= decoded-json re-decoded-json))))

(defn timed-tests [tests]
  (let [start (System/nanoTime)]
    (dotimes [i 1000]
      (doseq [t tests]
        (t)))
    (/ (double (- (System/nanoTime) start)) 1000000.0)))

#_(deftest test-performance
  (let [tests (->> (ns-publics 'cheshire.test.core)
                   (remove (comp (partial = 'test-performance) first))
                   (map second)
                   (filter (comp :test meta)))
        start (System/nanoTime)]
    (let [times (doall (map timed-tests (repeat 3 tests)))
          min-time (apply min times)
          max-time (apply max times)]
      (prn min-time max-time)
      (is (> 2000 min-time))
      (is (> 5200 max-time)))))

(deftest test-namespaced-keywords
  (is (= "{\"foo\":\"user/bar\"}"
         (json/encode {:foo :user/bar}))))
