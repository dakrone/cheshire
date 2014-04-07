(ns cheshire.test.generate
  (:require [cheshire.core :refer :all]
            [cheshire.generate :as gen])
  (:use [clojure.test]))

(defrecord Rec [a b c]
  gen/JSONable
  (to-json [this j]
    (gen/encode-map (select-keys this [:a :b]) j)))

(deftest test-jsonable
  (are [x y] (= (parse-string (generate-string x) true) y)
       (map->Rec {:a "a" :b 1 :c "x"}) {:a "a" :b 1}
       {:a [1 "2" (map->Rec {:a "a" :b 1 :c "x"})]} {:a [1 "2" {:a "a" :b 1}]}))
