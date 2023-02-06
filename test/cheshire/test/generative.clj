(ns cheshire.test.generative
  (:require
   [cheshire.core :refer [decode encode]]
   [clojure.data.generators]
   [clojure.test :refer [deftest is]]
   [clojure.test.generative :refer [defspec]]
   [clojure.test.generative.runner :as runner]))

(defn encode-equality [x]
  [x (decode (encode x))])

(defn encode-equality-keys [x]
  [x (decode (encode x) true)])

(defspec number-json-encoding
  (fn [a b c] [[a b c] (decode (encode [a b c]))])
  [^int a ^long b ^double c]
  (is (= (first %) (last %))))

(defspec bool-json-encoding
  encode-equality
  [^boolean a]
  (is (= (first %) (last %))))

(defspec symbol-json-encoding
  encode-equality
  [^symbol a]
  (is (= (str (first %)) (last %))))

(defspec keyword-json-encoding
  encode-equality
  [^keyword a]
  (is (= (name (first %)) (last %))))

(defspec map-json-encoding
  encode-equality
  [^{:tag (hash-map string (hash-map string (vec string 10) 10) 10)} a]
  (is (= (first %) (last %))))

(defspec map-keyword-json-encoding
  encode-equality-keys
  [^{:tag (hash-map keyword (hash-map keyword (list int 10) 10) 10)} a]
  (is (= (first %) (last %))))

(deftest ^{:generative true} t-generative
  (runner/run-suite {:nthreads (-> (Runtime/getRuntime) .availableProcessors)
                     :msec 25000
                     :progress (constantly true)}
                    (->> 'cheshire.test.generative ns-interns vals
                         (mapcat runner/get-tests))))
