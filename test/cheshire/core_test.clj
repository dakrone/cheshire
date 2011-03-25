(ns clj-json.core-test
  (:use clojure.test)
  (:require [clj-json.core :as json])
  (:import (java.io StringReader BufferedReader)))

(deftest test-string-round-trip
  (let [obj {"int" 3 "long" 52001110638799097 "bigint" 9223372036854775808
             "double" 1.23 "boolean" true "nil" nil "string" "string"
             "vec" [1 2 3] "map" {"a" "b"} "list" (list "a" "b")}]
    (is (= obj (json/parse-string (json/generate-string obj))))))

(deftest test-generate-accepts-float
  (is (= "3.14" (json/generate-string (float 3.14)))))
  
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

(deftest test-parsed-seq
  (let [br (BufferedReader. (StringReader. "1\n2\n3\n"))]
    (is (= (list 1 2 3) (json/parsed-seq br)))))

(deftest test-smile-round-trip
  (let [obj {"int" 3 "long" 52001110638799097 "bigint" 9223372036854775808
             "double" 1.23 "boolean" true "nil" nil "string" "string"
             "vec" [1 2 3] "map" {"a" "b"} "list" (list "a" "b")}]
    (is (= obj (json/parse-smile (json/generate-smile obj))))))
