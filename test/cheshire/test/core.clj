(ns cheshire.test.core
  (:use [clojure.test]
        [clojure.java.io :only [file reader]])
  (:require [cheshire.core :as json]
            [cheshire.exact :as json-exact]
            [cheshire.generate :as gen]
            [cheshire.factory :as fact]
            [cheshire.parse :as parse]
            [clojure.string :as str])
  (:import (com.fasterxml.jackson.core JsonGenerationException
                                       JsonParseException)
           (com.fasterxml.jackson.core.exc StreamConstraintsException)
           (java.io FileInputStream StringReader StringWriter
                    BufferedReader BufferedWriter
                    IOException)
           (java.sql Timestamp)
           (java.util Date UUID)))

(defn- str-of-len
  ([len]
   (str-of-len len "x"))
  ([len val]
   (apply str (repeat len val))))

(defn- nested-map [depth]
  (reduce (fn [acc n] {(str n) acc})
          {"0" "foo"}
          (range 1 depth)))

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
  (is (= test-obj (json/decode (json/encode test-obj)))))

(deftest test-generate-accepts-float
  (is (= "3.14" (json/encode 3.14))))

(deftest test-keyword-encode
  (is (= {"key" "val"}
         (json/decode (json/encode {:key "val"})))))

(deftest test-generate-set
  (is (= {"set" ["a" "b"]}
         (json/decode (json/encode {"set" #{"a" "b"}})))))

(deftest test-generate-empty-set
  (is (= {"set" []}
         (json/decode (json/encode {"set" #{}})))))

(deftest test-generate-empty-array
  (is (= {"array" []}
         (json/decode (json/encode {"array" []})))))

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

(deftest test-accepts-java-map
  (is (= {"foo" 1}
         (json/decode
          (json/encode (doto (java.util.HashMap.) (.put "foo" 1)))))))

(deftest test-accepts-java-list
  (is (= [1 2 3]
         (json/decode (json/encode (doto (java.util.ArrayList. 3)
                                     (.add 1)
                                     (.add 2)
                                     (.add 3)))))))

(deftest test-accepts-java-set
  (is (= {"set" [1 2 3]}
         (json/decode (json/encode {"set" (doto (java.util.HashSet. 3)
                                            (.add 1)
                                            (.add 2)
                                            (.add 3))})))))

(deftest test-accepts-empty-java-set
  (is (= {"set" []}
         (json/decode (json/encode {"set" (java.util.HashSet. 3)})))))

(deftest test-nil
  (is (nil? (json/decode nil true))))

(deftest test-parsed-seq
  (let [br (BufferedReader. (StringReader. "1\n2\n3\n"))]
    (is (= (list 1 2 3) (json/parsed-seq br)))))

(deftest test-smile-round-trip
  (is (= test-obj (json/parse-smile (json/generate-smile test-obj)))))

(def bin-obj {"byte-array" (byte-array (map byte [1 2 3]))})

(deftest test-round-trip-binary
  (doseq [[p g] {json/parse-string json/generate-string
                 json/parse-smile  json/generate-smile
                 json/parse-cbor   json/generate-cbor}]
    (is (let [roundtripped (p (g bin-obj))]
          ;; test value equality
          (is (= (->> bin-obj (get "byte-array") seq)
                 (->> roundtripped (get "byte-array") seq)))))))

(deftest test-smile-factory
  (binding [fact/*smile-factory* (fact/make-smile-factory {})]
    (is (= {"a" 1} (-> {:a 1}
                      json/generate-smile
                      json/parse-smile)))))

(deftest test-smile-duplicate-detection
  (let [smile-data (byte-array [0x3a 0x29 0x0a 0x01 ;; smile header
                                0xFa                ;; object start
                                0x80 0x61           ;; key a
                                0xC2                ;; value 1
                                0x80 0x61           ;; key a (again)
                                0xC4                ;; value 2
                                0xFB                ;; object end
                                ])]
    (binding [fact/*smile-factory* (fact/make-smile-factory {:strict-duplicate-detection false})]
      (is (= {"a" 2} (json/parse-smile smile-data))))
    (binding [fact/*smile-factory* (fact/make-smile-factory {:strict-duplicate-detection true})]
      (is (thrown? JsonParseException (json/parse-smile smile-data))))))

(deftest test-cbor-factory
  (binding [fact/*cbor-factory* (fact/make-cbor-factory {})]
    (is (= {"a" 1} (-> {:a 1}
                       json/generate-cbor
                       json/parse-cbor)))))

(deftest test-cbor-duplicate-detection
  (let [cbor-data (byte-array [0xbf         ;; object begin
                               0x61 0x61    ;; key a
                               0x01         ;; value 1
                               0x61 0x61    ;; key a (again)
                               0x02         ;; value 2
                               0xff         ;; object end
                               ])]
    (binding [fact/*cbor-factory* (fact/make-cbor-factory {:strict-duplicate-detection false})]
      (is (= {"a" 2} (json/parse-cbor cbor-data))))
    (binding [fact/*cbor-factory* (fact/make-cbor-factory {:strict-duplicate-detection true})]
      (is (thrown? JsonParseException (json/parse-cbor cbor-data))))))

(deftest test-aliases
  (is (= {"foo" "bar" "1" "bat" "2" "bang" "3" "biz"}
         (json/decode
          (json/encode
           {:foo "bar" 1 "bat" (long 2) "bang" (bigint 3) "biz"})))))

(deftest test-date
  (is (= {"foo" "1970-01-01T00:00:00Z"}
         (json/decode (json/encode {:foo (Date. (long 0))}))))
  (is (= {"foo" "1970-01-01"}
         (json/decode (json/encode {:foo (Date. (long 0))}
                                   {:date-format "yyyy-MM-dd"})))
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

(deftest test-char-literal
  (is (= "{\"foo\":\"a\"}" (json/encode {:foo \a}))))

(deftest test-streams
  (testing "parse-stream"
    (are [parsed parse parsee] (= parsed
                                  (parse (BufferedReader. (StringReader. parsee))))
      {"foo" "bar"} json/parse-stream "{\"foo\":\"bar\"}\n"
      {"foo" "bar"} json/parse-stream-strict "{\"foo\":\"bar\"}\n")

    (are [parsed parse parsee] (= parsed
                                  (with-open [rdr (StringReader. parsee)]
                                    (parse rdr true)))
      {(keyword "foo baz") "bar"} json/parse-stream "{\"foo baz\":\"bar\"}\n"
      {(keyword "foo baz") "bar"} json/parse-stream-strict "{\"foo baz\":\"bar\"}\n"))

  (testing "generate-stream"
    (let [sw (StringWriter.)
          bw (BufferedWriter. sw)]
      (json/generate-stream {"foo" "bar"} bw)
      (is (= "{\"foo\":\"bar\"}" (.toString sw))))
    (let [sw (StringWriter.)
          bw (BufferedWriter. sw)]
      (json/generate-stream {:foo "It costs £100"} bw {:escape-non-ascii true})
      (is (= "{\"foo\":\"It costs \\u00A3100\"}" (.toString sw))))
    (let [sw (StringWriter.)
          bw (BufferedWriter. sw)]
      (json/generate-stream {:foo "It costs £100"} bw {:escape-non-ascii false})
      (is (= "{\"foo\":\"It costs £100\"}" (.toString sw))))))

(deftest serial-writing
  (is (= "[\"foo\",\"bar\"]"
         (.toString
          (json/with-writer [(StringWriter.) nil]
            (json/write [] :start)
            (json/write "foo")
            (json/write "bar")
            (json/write [] :end)))))
  (is (= "[1,[2,3],4]"
         (.toString
          (json/with-writer [(StringWriter.) nil]
            (json/write [1 [2]] :start-inner)
            (json/write 3)
            (json/write [] :end)
            (json/write 4)
            (json/write [] :end)))))
  (is (= "{\"a\":1,\"b\":2,\"c\":3}"
         (.toString
          (json/with-writer [(StringWriter.) nil]
            (json/write {:a 1} :start)
            (json/write {:b 2} :bare)
            (json/write {:c 3} :end)))))
  (is (= (str "[\"start\",\"continue\",[\"implicitly-nested\"],"
              "[\"explicitly-nested\"],\"flatten\",\"end\"]")
         (.toString
          (json/with-writer [(StringWriter.) nil]
            (json/write ["start"] :start)
            (json/write "continue")
            (json/write ["implicitly-nested"])
            (json/write ["explicitly-nested"] :all)
            (json/write ["flatten"] :bare)
            (json/write ["end"] :end)))))
  (is (= "{\"head\":\"head info\",\"data\":[1,2,3],\"tail\":\"tail info\"}"
         (.toString
          (json/with-writer [(StringWriter.) nil]
            (json/write {:head "head info" :data []} :start-inner)
            (json/write 1)
            (json/write 2)
            (json/write 3)
            (json/write [] :end)
            (json/write {:tail "tail info"} :end))))))

(deftest test-multiple-objs-in-file
  (is (= {"one" 1, "foo" "bar"}
         (first (json/parsed-seq (reader "test/multi.json")))))
  (is (= {"two" 2, "foo" "bar"}
         (second (json/parsed-seq (reader "test/multi.json")))))
  (with-open [s (FileInputStream. (file "test/multi.json"))]
    (let [r (reader s)]
      (is (= [{"one" 1, "foo" "bar"} {"two" 2, "foo" "bar"}]
             (json/parsed-seq r))))))

(deftest test-jsondotorg-pass1
  (let [string (slurp "test/pass1.json")
        decoded-json (json/decode string)
        encoded-json (json/encode decoded-json)
        re-decoded-json (json/decode encoded-json)]
    (is (= decoded-json re-decoded-json))))

(deftest test-namespaced-keywords
  (is (= "{\"foo\":\"user/bar\"}"
         (json/encode {:foo :user/bar})))
  (is (= {:foo/bar "baz/eggplant"}
         (json/decode (json/encode {:foo/bar :baz/eggplant}) true))))

(deftest test-array-coerce-fn
  (is (= {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}
         (json/decode
          (json/encode {"set" #{"a" "b"} "array" ["a" "b"] "map" {"a" 1}}) false
          (fn [field-name] (if (= "set" field-name) #{} []))))))

(deftest t-symbol-encoding-for-non-resolvable-symbols
  (is (= "{\"bar\":\"clojure.core/pam\",\"foo\":\"clojure.core/map\"}"
         (json/encode (sorted-map :foo 'clojure.core/map :bar 'clojure.core/pam))))
  (is (= "{\"bar\":\"clojure.core/pam\",\"foo\":\"foo.bar/baz\"}"
         (json/encode (sorted-map :foo 'foo.bar/baz :bar 'clojure.core/pam)))))

(deftest t-bindable-factories-auto-close-source
  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:auto-close-source false})]
    (let [br (BufferedReader. (StringReader. "123"))]
      (is (= 123 (json/parse-stream br)))
      (is (= -1 (.read br)))))
  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:auto-close-source true})]
    (let [br (BufferedReader. (StringReader. "123"))]
      (is (= 123 (json/parse-stream br)))
      (is (thrown? IOException (.read br))))))

(deftest t-bindable-factories-allow-comments
  (let [s "{\"a\": /* comment */ 1, // comment\n \"b\": 2}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                   {:allow-comments true})]
      (is (= {"a" 1 "b" 2} (json/decode s))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                   {:allow-comments false})]
      (is (thrown? JsonParseException (json/decode s))))))

(deftest t-bindable-factories-allow-unquoted-field-names
  (let [s "{a: 1, b: 2}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                   {:allow-unquoted-field-names true})]
      (is (= {"a" 1 "b" 2} (json/decode s))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                   {:allow-unquoted-field-names false})]
      (is (thrown? JsonParseException (json/decode s))))))

(deftest t-bindable-factories-allow-single-quotes
  (doseq [s ["{'a': \"one\", 'b': \"two\"}"
             "{\"a\": 'one', \"b\": 'two'}"
             "{'a': 'one', 'b': 'two'}"]]
    (testing s
      (binding [fact/*json-factory* (fact/make-json-factory
                                      {:allow-single-quotes true})]
        (is (= {"a" "one" "b" "two"} (json/decode s))))
      (binding [fact/*json-factory* (fact/make-json-factory
                                      {:allow-single-quotes false})]
        (is (thrown? JsonParseException (json/decode s)))))))

(deftest t-bindable-factories-allow-unquoted-control-chars
  (let [s "{\"a\": \"one\ntwo\"}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-unquoted-control-chars true})]
      (is (= {"a" "one\ntwo"} (json/decode s))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-unquoted-control-chars false})]
      (is (thrown? JsonParseException (json/decode s))))))

(deftest t-bindable-factories-allow-backslash-escaping-any-char
  (let [s "{\"a\": 00000000001}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-numeric-leading-zeros true})]
      (is (= {"a" 1} (json/decode s))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-numeric-leading-zeros false})]
      (is (thrown? JsonParseException (json/decode s))))))

(deftest t-bindable-factories-allow-numeric-leading-zeros
  (let [s "{\"a\": \"\\o\\n\\e\"}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-backslash-escaping true})]
      (is (= {"a" "o\ne"} (json/decode s))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-backslash-escaping false})]
      (is (thrown? JsonParseException (json/decode s))))))

(deftest t-bindable-factories-non-numeric-numbers
  (let [s "{\"foo\":NaN}"]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-non-numeric-numbers true})]
      (is (= (type Double/NaN)
             (type (:foo (json/decode s true))))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:allow-non-numeric-numbers false})]
      (is (thrown? JsonParseException (json/decode s true))))))

(deftest t-bindable-factories-optimization-opts
  (let [s "{\"a\": \"foo\"}"]
    (doseq [opts [{:intern-field-names true}
                  {:intern-field-names false}
                  {:canonicalize-field-names true}
                  {:canonicalize-field-names false}]]
      (binding [fact/*json-factory* (fact/make-json-factory opts)]
        (is (= {"a" "foo"} (json/decode s)))))))

(deftest t-bindable-factories-quoteless
  (binding [fact/*json-factory* (fact/make-json-factory
                                  {:quote-field-names true})]
    (is (= "{\"a\":\"foo\"}" (json/encode {:a "foo"}))))
  (binding [fact/*json-factory* (fact/make-json-factory
                                  {:quote-field-names false})]
    (is (= "{a:\"foo\"}" (json/encode {:a "foo"})))))

(deftest t-bindable-factories-strict-duplicate-detection
  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:strict-duplicate-detection true})]
    (is (thrown? JsonParseException
                 (json/decode "{\"a\": 1, \"b\": 2, \"a\": 3}"))))

  (binding [fact/*json-factory* (fact/make-json-factory
                                 {:strict-duplicate-detection false})]
    (is (= {"a" 3 "b" 2}
           (json/decode "{\"a\": 1, \"b\": 2, \"a\": 3}")))))

(deftest t-bindable-factories-max-input-document-length
  (let [edn {"a" (apply str (repeat 10000 "x"))}
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-document-length (count sample-data)})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    ;; as per Jackson docs, limit is inexact, so dividing input length by 2 should do the trick
                                    {:max-input-document-length (/ (count sample-data) 2)})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)document length .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-max-input-token-count
  ;; A token is a single unit of input, such as a number, a string, an object start or end, or an array start or end.
  (let [edn {"1" 2 "3" 4}
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-token-count 6})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-token-count 5})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)token count .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-max-input-name-length
  (let [k "somekey"
        edn {k 1}
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-name-length (count k)})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-name-length (dec (count k))})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)name .* exceeds"
            (json/decode sample-data)))))
  (let [default-limit (:max-input-name-length fact/default-factory-options)]
    (let [k (str-of-len default-limit)
          edn {k 1}
          sample-data (json/encode edn)]
      (is (= edn (json/decode sample-data))))
    (let [k (str-of-len (inc default-limit))
          sample-data (json/encode {k 1})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)name .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-input-nesting-depth
  (let [edn (nested-map 100)
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-nesting-depth 100})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-nesting-depth 99})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)nesting depth .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-max-input-number-length
  (let [num 123456789
        edn {"foo" num}
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-number-length (-> num str count)})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-number-length (-> num str count dec)})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)number value length .* exceeds"
            (json/decode sample-data)))))
  (let [default-limit (:max-input-number-length fact/default-factory-options)]
    (let [num (bigint (str-of-len default-limit 2))
          edn {"foo" num}
          sample-data (json/encode edn)]
      (is (= edn (json/decode sample-data))))
    (let [num (bigint (str-of-len (inc default-limit) 2))
          sample-data (json/encode {"foo" num})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)number value length .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-max-input-string-length
  (let [big-string (str-of-len 40000000)
        edn {"big-string" big-string}
        sample-data (json/encode edn)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-string-length (count big-string)})]
      (is (= edn (json/decode sample-data))))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-input-string-length (dec (count big-string))})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)string value length .* exceeds"
            (json/decode sample-data)))))
  (let [default-limit (:max-input-string-length fact/default-factory-options)]
    (let [big-string (str-of-len default-limit)
          edn {"big-string" big-string}
          sample-data (json/encode edn)]
      (is (= edn (json/decode sample-data))))
    (let [big-string (str-of-len (inc default-limit))
          sample-data (json/encode {"big-string" big-string})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)string value length .* exceeds"
            (json/decode sample-data))))))

(deftest t-bindable-factories-max-output-nesting-depth
  (let [edn (nested-map 100)]
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-output-nesting-depth 100})]
      (is (str/includes? (json/encode edn) "\"99\"")))
    (binding [fact/*json-factory* (fact/make-json-factory
                                    {:max-output-nesting-depth 99})]
      (is (thrown-with-msg?
            StreamConstraintsException #"(?i)nesting depth .* exceeds"
            (json/encode edn))))))

(deftest t-persistent-queue
  (let [q (conj (clojure.lang.PersistentQueue/EMPTY) 1 2 3)]
    (is (= q (json/decode (json/encode q))))))

(deftest t-pretty-print
  (is (= (str "{\n  \"bar\" : [ {\n    \"baz\" : 2\n  }, "
              "\"quux\", [ 1, 2, 3 ] ],\n  \"foo\" : 1\n}")
         (json/encode (sorted-map :foo 1 :bar [{:baz 2} :quux [1 2 3]])
                      {:pretty true}))))

(deftest t-pretty-print-illegal-argument
  ; just expecting this not to throw
  (json/encode {:foo "bar"}
               {:pretty []})
  (json/encode {:foo "bar"}
               {:pretty nil}))

(deftest t-custom-pretty-print-with-defaults
  (let [test-obj (sorted-map :foo 1 :bar {:baz [{:ulu "mulu"} {:moot "foo"} 3]} :quux :blub)
        pretty-str-default (json/encode test-obj {:pretty true})
        pretty-str-custom (json/encode test-obj {:pretty {}})]
    (is (= pretty-str-default pretty-str-custom))
    (when-not (= pretty-str-default pretty-str-custom)
      ; print for easy comparison
      (println "; default pretty print")
      (println pretty-str-default)
      (println "; custom pretty print with default options")
      (println pretty-str-custom))))

(deftest t-custom-pretty-print-with-non-defaults
  (let [test-obj (sorted-map :foo 1 :bar {:baz [{:ulu "mulu"} {:moot "foo"} 3]} :quux :blub)
        test-opts {:pretty {:indentation 4
                            :indent-arrays? false
                            :before-array-values ""
                            :after-array-values ""
                            :object-field-value-separator ": "}}
        expected (str "{\n"
                      "    \"bar\": {\n"
                      "        \"baz\": [{\n"
                      "            \"ulu\": \"mulu\"\n"
                      "        }, {\n"
                      "            \"moot\": \"foo\"\n"
                      "        }, 3]\n"
                      "    },\n"
                      "    \"foo\": 1,\n"
                      "    \"quux\": \"blub\"\n"
                      "}")
        pretty-str (json/encode test-obj test-opts)]

    ; just to be easy on the eyes in case of error
    (when-not (= expected pretty-str)
      (println "; pretty print with options - actual")
      (println pretty-str)
      (println "; pretty print with options - expected")
      (println expected))
    (is (= expected pretty-str))))

(deftest t-custom-pretty-print-with-noident-objects
  (let [test-obj  [{:foo 1 :bar 2} {:foo 3 :bar 4}]
        test-opts {:pretty {:indent-objects? false}}
        expected (str "[ { \"foo\" : 1, \"bar\" : 2 }, "
                      "{ \"foo\" : 3, \"bar\" : 4 } ]")
        pretty-str (json/encode test-obj test-opts)]
    ; just to be easy on the eyes in case of error
    (when-not (= expected pretty-str)
      (println "; pretty print with options - actual")
      (println pretty-str)
      (println "; pretty print with options - expected")
      (println expected))
    (is (= expected pretty-str))))

(deftest t-unicode-escaping
  (is (= "{\"foo\":\"It costs £100\"}"
         (json/encode {:foo "It costs £100"} {:escape-non-ascii false})))
  (is (= "{\"foo\":\"It costs \\u00A3100\"}"
         (json/encode {:foo "It costs £100"} {:escape-non-ascii true}))))

(deftest t-custom-keyword-fn
  (is (= {:FOO "bar"} (json/decode "{\"foo\": \"bar\"}"
                                   (fn [k] (keyword (.toUpperCase k))))))
  (is (= {"foo" "bar"} (json/decode "{\"foo\": \"bar\"}" nil)))
  (is (= {"foo" "bar"} (json/decode "{\"foo\": \"bar\"}" false)))
  (is (= {:foo "bar"} (json/decode "{\"foo\": \"bar\"}" true))))

(deftest t-custom-encode-key-fn
  (is (= "{\"FOO\":\"bar\"}"
         (json/encode {:foo :bar}
                      {:key-fn (fn [k] (.toUpperCase (name k)))}))))

(deftest test-add-remove-encoder
  (gen/remove-encoder java.net.URL)
  (gen/add-encoder java.net.URL gen/encode-str)
  (is (= "\"http://foo.com\""
         (json/encode (java.net.URL. "http://foo.com"))))
  (gen/remove-encoder java.net.URL)
  (is (thrown? JsonGenerationException
               (json/encode (java.net.URL. "http://foo.com")))))

(defprotocol TestP
  (foo [this] "foo method"))

(defrecord TestR [state])

(extend TestR
  TestP
  {:foo (constantly "bar")})

(deftest t-custom-protocol-encoder
  (let [rec (TestR. :quux)]
    (is (= {:state "quux"} (json/decode (json/encode rec) true)))
    (gen/add-encoder cheshire.test.core.TestR
                     (fn [obj jg]
                       (.writeString jg (foo obj))))
    (is (= "bar" (json/decode (json/encode rec))))
    (gen/remove-encoder cheshire.test.core.TestR)
    (is (= {:state "quux"} (json/decode (json/encode rec) true)))))

(defprotocol CTestP
  (thing [this] "thing method"))
(defrecord CTestR [state])
(extend CTestR
  CTestP
  {:thing (constantly "thing")})

(deftest t-custom-helpers
  (let [thing (CTestR. :state)
        remove #(gen/remove-encoder CTestR)]
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-nil nil jg)))
    (is (= nil (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-str "foo" jg)))
    (is (= "foo" (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-number 5 jg)))
    (is (= 5 (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-long 4 jg)))
    (is (= 4 (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-int 3 jg)))
    (is (= 3 (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-ratio 1/2 jg)))
    (is (= 0.5 (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-seq [:foo :bar] jg)))
    (is (= ["foo" "bar"] (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-date (Date. (long 0)) jg)))
    (binding [gen/*date-format* "yyyy-MM-dd'T'HH:mm:ss'Z'"]
      (is (= "1970-01-01T00:00:00Z" (json/decode (json/encode thing) true))))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-bool true jg)))
    (is (= true (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-named :foo jg)))
    (is (= "foo" (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-map {:foo "bar"} jg)))
    (is (= {:foo "bar"} (json/decode (json/encode thing) true)))
    (remove)
    (gen/add-encoder CTestR (fn [obj jg] (gen/encode-symbol 'foo jg)))
    (is (= "foo" (json/decode (json/encode thing) true)))
    (remove)))

(deftest t-float-encoding
  (is (= "{\"foo\":0.01}" (json/encode {:foo (float 0.01)}))))

(deftest t-non-const-bools
  (is (= {:a 1} (json/decode "{\"a\": 1}" (Boolean. true)))))

(deftest t-invalid-json
  (let [invalid-json-message "Invalid JSON, expected exactly one parseable object but multiple objects were found"]
    (are [x y] (= x (try
                      y
                      (catch Exception e
                        (.getMessage e))))
      invalid-json-message (json-exact/decode "{\"foo\": 1}asdf")
      invalid-json-message (json-exact/decode "{\"foo\": 123}null")
      invalid-json-message (json-exact/decode  "\"hello\" : 123}")
      {"foo" 1} (json/decode "{\"foo\": 1}")
      invalid-json-message (json-exact/decode-strict "{\"foo\": 1}asdf")
      invalid-json-message (json-exact/decode-strict "{\"foo\": 123}null")
      invalid-json-message (json-exact/decode-strict  "\"hello\" : 123}")
      {"foo" 1} (json/decode-strict "{\"foo\": 1}"))))
