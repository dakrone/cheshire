(ns cheshire.test.custom
  (:use [clojure.test]
        [clojure.java.io :only [reader]])
  (:require [cheshire.custom :as json] :reload
            [cheshire.factory :as fact]
            [cheshire.parse :as parse])
  (:import (java.io StringReader StringWriter
                    BufferedReader BufferedWriter)
           (java.sql Timestamp)
           (java.util Date UUID)))

(def test-obj {"int" 3 "long" (long -2147483647) "boolean" true
               "LongObj" (Long/parseLong "2147483647") "double" 1.23
               "nil" nil "string" "string" "vec" [1 2 3] "map" {"a" "b"}
               "list" (list "a" "b") "short" (short 21) "byte" (byte 3)})

(deftest t-ratio
  (let [n 1/2]
    (is (= (double n) (:num (json/decode (json/encode {:num n}) true))))))

(deftest t-long-wrap-around
  (is (= 2147483648 (json/decode (json/encode 2147483648)))))

(deftest t-bigint
  (let [n 9223372036854775808]
    (is (= n (:num (json/decode (json/encode {:num n}) true))))))

(deftest t-biginteger
  (let [n (BigInteger. "42")]
    (is (= n (:num (json/decode (json/encode {:num n}) true))))))

(deftest t-bigdecimal
  (let [n (BigDecimal. "42.5")]
    (is (= (.doubleValue n) (:num (json/decode (json/encode {:num n}) true))))
    (binding [parse/*use-bigdecimals?* true]
      (is (= n (:num (json/decode (json/encode {:num n}) true)))))))

(deftest test-string-round-trip
  (is (= test-obj (json/decode (json/encode* test-obj)))))

(deftest test-generate-accepts-float
  (is (= "3.14" (json/encode 3.14))))

(deftest test-keyword-encode
  (is (= {"key" "val"}
         (json/decode (json/encode {:key "val"})))))

(deftest test-generate-set
  (is (= {"set" ["a" "b"]}
         (json/decode (json/encode {"set" #{"a" "b"}})))))

(deftest test-key-coercion
  (is (= {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"}
         (json/decode
          (json/encode
           {:foo "bar" 1 "bat" (long 2) "bang" (bigint 3) "biz"})))))

(deftest test-keywords
  (is (= {:foo "bar" :bat 1}
         (json/decode (json/encode {:foo "bar" :bat 1}) true))))

(deftest test-symbols
  (is (= {"foo" "clojure.core/map"}
         (json/decode (json/encode {"foo" 'clojure.core/map})))))

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
           {:foo (Date. (long 0))} {:date-format "yyyy-MM-dd"})))
      "encode with given date format"))

(deftest test-sql-timestamp
  (is (= {"foo" "1970-01-01T00:00:00Z"}
         (json/decode (json/encode {:foo (Timestamp. (long 0))}))))
  (is (= {"foo" "1970-01-01"}
         (json/decode (json/encode {:foo (Timestamp. (long 0))}
                                   {:date-format "yyyy-MM-dd"})))
      "encode with given date format"))

(deftest test-uuid
  (let [id (UUID/randomUUID)
        id-str (str id)]
    (is (= {"foo" id-str} (json/decode (json/encode {:foo id}))))))

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

(deftest test-multiple-objs-in-file
  (is (= {"one" 1, "foo" "bar"}
         (first (json/parsed-seq (reader "test/multi.json")))))
  (is (= {"two" 2, "foo" "bar"}
         (second (json/parsed-seq (reader "test/multi.json"))))))

(deftest test-jsondotorg-pass1
  (let [string (slurp "test/pass1.json")
        decoded-json (json/decode string)
        encoded-json (json/encode decoded-json)
        re-decoded-json (json/decode encoded-json)]
    (is (= decoded-json re-decoded-json))))

(deftest test-add-remove-encoder
  (json/remove-encoder java.net.URL)
  (json/add-encoder java.net.URL json/encode-str)
  (is (= "\"http://foo.com\""
         (json/encode (java.net.URL. "http://foo.com"))))
  (json/remove-encoder java.net.URL)
  (is (thrown? IllegalArgumentException
               (json/encode (java.net.URL. "http://foo.com")))))

;; Test that default encoders can be bypassed if so desired.
(deftest test-shadowing-default-encoder
  (json/remove-encoder java.util.Date)
  (json/add-encoder java.util.Date
                    (fn [d jg] (json/encode-str "foo" jg)))
  (is (= "\"foo\"" (json/encode* (java.util.Date.))))
  (is (= "\"foo\"" (json/encode* :foo)))
  (json/remove-encoder java.util.Date)
  (json/add-encoder java.util.Date json/encode-date)
  (is (json/encode (java.util.Date.))
      "shouldn't throw an exception after adding back the default."))

(deftest test-namespaced-keywords
  (is (= "{\"foo\":\"user/bar\"}"
         (json/encode {:foo :user/bar}))))

(deftest test-array-coerce-fn
  (is (= {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}
         (json/decode
          (json/encode {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}) false
          (fn [field-name] (if (= "set" field-name) #{} []))))))

(deftest t-symbol-encoding-for-non-resolvable-symbols
  (is (= "{\"foo\":\"clojure.core/map\",\"bar\":\"clojure.core/pam\"}"
         (json/encode {:foo 'clojure.core/map :bar 'clojure.core/pam})))
  (is (= "{\"foo\":\"foo.bar/baz\",\"bar\":\"clojure.core/pam\"}"
         (json/encode {:foo 'foo.bar/baz :bar 'clojure.core/pam}))))

(deftest t-bindable-factories
  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:allow-non-numeric-numbers true})]
    (is (= (type Double/NaN)
           (type (:foo (json/decode "{\"foo\":NaN}" true)))))))

(deftest t-persistent-queue
  (let [q (conj (clojure.lang.PersistentQueue/EMPTY) 1 2 3)]
    (is (= q (json/decode (json/encode q))))
    (is (= q (json/decode (json/encode* q))))))

(deftest t-namespaced-keywords
  (is (= {:foo/bar "baz/eggplant"}
         (json/decode (json/encode {:foo/bar :baz/eggplant}) true))))

(deftest t-pretty-print
  (is (= (str "{\n  \"foo\" : 1,\n  \"bar\" : [ {\n    \"baz\" : 2\n  }, "
              "\"quux\", [ 1, 2, 3 ] ]\n}")
         (json/encode {:foo 1 :bar [{:baz 2} :quux [1 2 3]]}
                      {:pretty true}))))
