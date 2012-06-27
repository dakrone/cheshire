(ns cheshire.test.core
  (:use [expectations.scenarios]
        [clojure.java.io :only [file reader]])
  (:require [cheshire.core :as json]
            [cheshire.factory :as fact]
            [cheshire.parse :as parse])
  (:import (java.io FileInputStream StringReader StringWriter
                    BufferedReader BufferedWriter)
           (java.sql Timestamp)
           (java.util Date UUID)))

(def test-obj {"int" 3 "long" (long -2147483647) "boolean" true
               "LongObj" (Long/parseLong "2147483647") "double" 1.23
               "nil" nil "string" "string" "vec" [1 2 3] "map" {"a" "b"}
               "list" (list "a" "b") "short" (short 21) "byte" (byte 3)})

(scenario
  :reminder "ratios should correctly encode"
  (let [n 1/2]
    (expect (double n) (:num (json/decode (json/encode {:num n}) true)))))

(scenario
  :reminder "different types should encode fine"
  (expect 2147483647 (json/decode (json/encode 2147483647)))
  (given [x y] (expect x (json/decode (json/encode y)))
    9223372036854775808 9223372036854775808
    (BigInteger. "42") (BigInteger. "42")
    test-obj test-obj
    {"key" "val"} {:key "val"}
    {"set" ["a" "b"]} {"set" #{"a" "b"}}
    {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"} {:foo "bar" 1 "bat"
                                                  (long 2) "bang"
                                                  (bigint 3) "biz"}))

(scenario
  :reminder "exceptions should throw JsonGenerationExceptions"
  (expect com.fasterxml.jackson.core.JsonGenerationException
    (json/encode (.getBytes "foo"))))

(scenario
  :reminder "t-long-wrap-around"
  (expect 2147483648 (json/decode (json/encode 2147483648))))

(scenario
  :reminder "t-bigint"
  (let [n 9223372036854775808]
    (expect n (:num (json/decode (json/encode {:num n}) true)))))

(scenario
  :reminder "t-bigdecimal"
  (let [n (BigDecimal. "42.5")]
    (expect (.doubleValue n) (:num (json/decode (json/encode {:num n}) true)))
    (binding [parse/*use-bigdecimals?* true]
      (expect n (:num (json/decode (json/encode {:num n}) true))))))

(scenario
  :reminder "t-generate-accepts-float"
  (expect "3.14" (json/encode 3.14)))

(scenario
  :reminder "t-keywords"
  (expect
    {:foo "bar" :bat 1}
    (json/decode (json/encode {:foo "bar" :bat 1}) true)))

(scenario
  :reminder "t-symbols"
  (expect
    {"foo" "clojure.core/map"}
    (json/decode (json/encode {"foo" 'clojure.core/map}))))

(scenario
  :reminder "t-accepts-java-map"
  (expect
    {"foo" 1}
    (json/decode
     (json/encode (doto (java.util.HashMap.) (.put "foo" 1))))))

(scenario
  :reminder "t-accepts-java-list"
  (expect
    [1 2 3]
    (json/decode (json/encode (doto (java.util.ArrayList. 3)
                                (.add 1)
                                (.add 2)
                                (.add 3))))))

(scenario
  :reminder "t-accepts-java-set"
  (expect
    {"set" [1 2 3]}
    (json/decode (json/encode {"set" (doto (java.util.HashSet. 3)
                                       (.add 1)
                                       (.add 2)
                                       (.add 3))}))))

(scenario
  :reminder "test-nil"
  (expect nil? (json/decode nil true)))

(scenario
  :reminder "test-parsed-seq"
  (let [br (BufferedReader. (StringReader. "1\n2\n3\n"))]
    (expect (list 1 2 3) (json/parsed-seq br))))

(scenario
  :reminder "test-smile-round-trip"
  (expect test-obj (json/parse-smile (json/generate-smile test-obj))))

(scenario
  :reminder "test-aliases"
  (expect
    {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"}
    (json/decode
     (json/encode
      {:foo "bar" 1 "bat" (long 2) "bang" (bigint 3) "biz"}))))

(scenario
  :reminder "test-date"
  (expect
    {"foo" "1970-01-01T00:00:00Z"}
    (json/decode (json/encode {:foo (Date. (long 0))})))
  (expect
    {"foo" "1970-01-01"}
    (json/decode (json/encode {:foo (Date. (long 0))}
                              {:date-format "yyyy-MM-dd"}))))

(scenario
  :reminder "test-sql-timestamp"
  (expect
    {"foo" "1970-01-01T00:00:00Z"}
    (json/decode (json/encode {:foo (Timestamp. (long 0))})))
  (expect
    {"foo" "1970-01-01"}
    (json/decode (json/encode {:foo (Timestamp. (long 0))}
                              {:date-format "yyyy-MM-dd"}))))

(scenario
  :reminder "test-uuid"
  (let [id (UUID/randomUUID)
        id-str (str id)]
    (expect {"foo" id-str} (json/decode (json/encode {:foo id})))))

(scenario
  :reminder "test-streams"
  (expect
    {"foo" "bar"}
    (json/parse-stream
     (BufferedReader. (StringReader. "{\"foo\":\"bar\"}\n"))))
  (let [sw (StringWriter.)
        bw (BufferedWriter. sw)]
    (json/generate-stream {"foo" "bar"} bw)
    (expect "{\"foo\":\"bar\"}" (.toString sw)))
  (expect
    {(keyword "foo baz") "bar"}
    (with-open [rdr (StringReader. "{\"foo baz\":\"bar\"}\n")]
      (json/parse-stream rdr true))))

(scenario
  :reminder "test-multiple-objs-in-file"
  (expect
    {"one" 1, "foo" "bar"}
    (first (json/parsed-seq (reader "test/multi.json"))))
  (expect
    {"two" 2, "foo" "bar"}
    (second (json/parsed-seq (reader "test/multi.json"))))
  (with-open [s (FileInputStream. (file "test/multi.json"))]
    (let [r (reader s)]
      (expect
        [{"one" 1, "foo" "bar"} {"two" 2, "foo" "bar"}]
        (json/parsed-seq r)))))

(scenario
  :reminder "test-jsondotorg-pass1"
  (let [string (slurp "test/pass1.json")
        decoded-json (json/decode string)
        encoded-json (json/encode decoded-json)
        re-decoded-json (json/decode encoded-json)]
    (expect decoded-json re-decoded-json)))

(scenario
  :reminder "test-namespaced-keywords"
  (expect
    "{\"foo\":\"user/bar\"}"
    (json/encode {:foo :user/bar})))

(scenario
  :reminder "test-array-coerce-fn"
  (expect
    {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}
    (json/decode
     (json/encode {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}) false
     (fn [field-name] (if (= "set" field-name) #{} [])))))

(scenario
  :reminder "t-symbol-encoding-for-non-resolvable-symbols"
  (expect
    "{\"foo\":\"clojure.core/map\",\"bar\":\"clojure.core/pam\"}"
    (json/encode {:foo 'clojure.core/map :bar 'clojure.core/pam}))
  (expect
    "{\"foo\":\"foo.bar/baz\",\"bar\":\"clojure.core/pam\"}"
    (json/encode {:foo 'foo.bar/baz :bar 'clojure.core/pam})))

(scenario
  :reminder "t-bindable-factories"
  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:allow-non-numeric-numbers true})]
    (expect
      {:foo Double/NaN}
      (in (json/decode "{\"foo\":NaN}" true)))))

(scenario
  :reminder "t-persistent-queue"
  (let [q (conj (clojure.lang.PersistentQueue/EMPTY) 1 2 3)]
    (expect q (json/decode (json/encode q)))))

(scenario
  :reminder "t-namespaced-keywords"
  (expect
    {:foo/bar "baz/eggplant"}
    (json/decode (json/encode {:foo/bar :baz/eggplant}) true)))

(scenario
  :reminder "t-pretty-print"
  (expect
    (str "{\n  \"foo\" : 1,\n  \"bar\" : [ {\n    \"baz\" : 2\n  }, "
         "\"quux\", [ 1, 2, 3 ] ]\n}")
    (json/encode {:foo 1 :bar [{:baz 2} :quux [1 2 3]]}
                 {:pretty true})))
