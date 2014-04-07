(ns cheshire.test.generate
  (:require [cheshire.core :refer :all]
            [cheshire.generate :as gen])
  (:use [clojure.test]))

(defrecord Derive [a b c]
  gen/JSONable
  (to-json [this j]
    (gen/encode-map (select-keys this [:a :b]) j)))

(defrecord Extend [a b c])

(extend-protocol gen/JSONable
  Extend
  (to-json [this j]
    (gen/encode-map (select-keys this [:a :b]) j)))

(deftest test-jsonable
  (doseq [constr [->Derive ->Extend]]
    (are [x y] (= (parse-string (generate-string x) true) y)
         (constr "a" 1 "x") {:a "a" :b 1}
         {:a [1 "2" (constr "a" 1 "x")]} {:a [1 "2" {:a "a" :b 1}]})))
