(ns cheshire.test.custom
  (:use [clojure.test]
        [clojure.java.io :only [reader]])
  (:require [cheshire.custom :as json] :reload)
  (:import (java.io StringReader StringWriter
                    BufferedReader BufferedWriter)
           (java.sql Timestamp)
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

(deftest test-sql-timestamp
  (is (= {"foo" "1970-01-01T00:00:00Z"}
         (json/decode
          (json/encode
           {:foo (Timestamp. (long 0))}))))
  (is (= {"foo" "1970-01-01"}
         (json/decode
          (json/encode
           {:foo (Timestamp. (long 0))} "yyyy-MM-dd")))
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

(deftest test-add-remove-encoder
  (json/remove-encoder java.net.URL)
  (json/add-encoder java.net.URL json/encode-str)
  (is (= "\"http://foo.com\""
         (json/generate-string (java.net.URL. "http://foo.com"))))
  (json/remove-encoder java.net.URL)
  (is (thrown? IllegalArgumentException
               (json/generate-string (java.net.URL. "http://foo.com")))))

;; Test that default encoders can be removed if so desired.
(deftest test-shadowing-default-encoder
  (json/remove-encoder java.util.Date)
  (json/add-encoder java.util.Date
                    (fn [d jg] (json/encode-str "foo" jg)))
  (is (= "\"foo\"" (json/generate-string (java.util.Date.))))
  (json/remove-encoder java.util.Date)
  (json/add-encoder java.util.Date json/encode-date)
  (is (json/generate-string (java.util.Date.))
      "shouldn't throw an exception after adding back the default."))

(deftest test-namespaced-keywords
  (is (= "{\"foo\":\"user/bar\"}"
         (json/encode {:foo :user/bar}))))
