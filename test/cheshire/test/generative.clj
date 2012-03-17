(ns cheshire.test.generative
  (:use [cheshire.core]
        [clojure.test.generative]
        [clojure.test :only [deftest is]]))

;; determines whether generative stuff is printed to stdout
(def verbose true)

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
  ;; I want the seeds to change every time, set the number higher if
  ;; you have more than 16 CPU cores
  (let [seeds (take 16 (repeatedly #(rand-int 1024)))]
    (when-not verbose
      (reset! report-fn identity))
    (println "Seeds:" seeds)
    (binding [*msec* 25000
              *seeds* seeds
              *verbose* false]
      (doall (map deref (test-namespaces 'cheshire.test.generative))))))
