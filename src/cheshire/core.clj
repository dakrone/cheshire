(ns cheshire.core
  "Main encoding and decoding namespace."
  (:require [cheshire.factory :as factory]
            [cheshire.generate :as gen]
            [cheshire.parse :as parse])
  (:import (com.fasterxml.jackson.core JsonParser JsonFactory
                                       JsonGenerator$Feature)
           (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (java.io StringWriter StringReader BufferedReader BufferedWriter
                    ByteArrayOutputStream)))

;; Generators
(defn ^String generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj]
     (generate-string obj nil))
  ([obj opt-map]
     (let [sw (StringWriter.)
           generator (.createJsonGenerator
                      ^JsonFactory (or factory/*json-factory*
                                       factory/json-factory) sw)]
       (when (:pretty opt-map)
         (.useDefaultPrettyPrinter generator))
       (when (:escape-non-ascii opt-map)
         (.enable generator JsonGenerator$Feature/ESCAPE_NON_ASCII))
       (gen/generate generator obj
                     (or (:date-format opt-map) factory/default-date-format)
                     (:ex opt-map)
                     (:key-fn opt-map))
       (.flush generator)
       (.toString sw))))

(defn ^String generate-stream
  "Returns a BufferedWriter for the given Clojure object with the.
  JSON-encoded data written to the writer. Takes an optional date
  format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj ^BufferedWriter writer]
     (generate-stream obj writer nil))
  ([obj ^BufferedWriter writer opt-map]
     (let [generator (.createJsonGenerator
                      ^JsonFactory (or factory/*json-factory*
                                       factory/json-factory) writer)]
       (when (:pretty opt-map)
         (.useDefaultPrettyPrinter generator))
       (when (:escape-non-ascii opt-map)
         (.enable generator JsonGenerator$Feature/ESCAPE_NON_ASCII))
       (gen/generate generator obj (or (:date-format opt-map)
                                       factory/default-date-format)
                     (:ex opt-map)
                     (:key-fn opt-map))
       (.flush generator)
       writer)))

(defn generate-smile
  "Returns a SMILE-encoded byte-array for the given Clojure object.
  Takes an optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj]
     (generate-smile obj nil))
  ([obj opt-map]
     (let [baos (ByteArrayOutputStream.)
           generator (.createJsonGenerator ^SmileFactory
                                           (or factory/*smile-factory*
                                               factory/smile-factory)
                                           baos)]
       (gen/generate generator obj (or (:date-format opt-map)
                                       factory/default-date-format)
                     (:ex opt-map)
                     (:key-fn opt-map))
       (.flush generator)
       (.toByteArray baos))))

;; Parsers
(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([string] (parse-string string nil nil))
  ([string key-fn] (parse-string string key-fn nil))
  ([string key-fn array-coerce-fn] (parse-string string key-fn array-coerce-fn nil))
  ([^String string key-fn array-coerce-fn field-predicate]
     (when string
       (parse/parse
        (.createJsonParser ^JsonFactory (or factory/*json-factory*
                                            factory/json-factory)
                           (StringReader. string))
        key-fn nil array-coerce-fn field-predicate))))

;; Parsing strictly
(defn parse-string-strict
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.

  Does not lazily parse top-level arrays."
  ([string] (parse-string-strict string nil nil))
  ([string key-fn] (parse-string-strict string key-fn nil))
  ([string key-fn array-coerce-fn] (parse-string-strict string key-fn array-coerce-fn nil))
  ([^String string key-fn array-coerce-fn field-predicate]
     (when string
       (parse/parse-strict
        (.createJsonParser ^JsonFactory (or factory/*json-factory*
                                            factory/json-factory)
                           (StringReader. string))
        key-fn array-coerce-fn field-predicate))))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. An optional key-fn argument can be either true (to
  coerce keys to keywords),false to leave them as strings, or a function to
  provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If laziness is needed, see parsed-seq."
  ([rdr] (parse-stream rdr nil nil nil))
  ([rdr key-fn] (parse-stream rdr key-fn nil nil))
  ([rdr key-fn array-coerce-fn] (parse-stream rdr key-fn array-coerce-fn nil))
  ([^BufferedReader rdr key-fn array-coerce-fn field-predicate]
     (when rdr
       (parse/parse
        (.createJsonParser ^JsonFactory (or factory/*json-factory*
                                            factory/json-factory) rdr)
        key-fn nil array-coerce-fn field-predicate))))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([bytes] (parse-smile bytes nil nil nil))
  ([bytes key-fn] (parse-smile bytes key-fn nil nil))
  ([^bytes bytes key-fn array-coerce-fn] (parse-smile bytes key-fn array-coerce-fn nil))
  ([^bytes bytes key-fn array-coerce-fn field-predicate]
     (when bytes
       (parse/parse
         (.createJsonParser ^SmileFactory (or factory/*smile-factory*
                                            factory/smile-factory) bytes)
         key-fn nil array-coerce-fn field-predicate))))

(def ^{:doc "Object used to determine end of lazy parsing attempt."}
  eof (Object.))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser key-fn array-coerce-fn field-predicate]
  (lazy-seq
   (let [elem (parse/parse-strict parser key-fn eof array-coerce-fn field-predicate)]
     (when-not (identical? elem eof)
       (cons elem (parsed-seq* parser key-fn array-coerce-fn field-predicate))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If non-laziness is needed, see parse-stream."
  ([reader] (parsed-seq reader nil nil nil))
  ([reader key-fn] (parsed-seq reader key-fn nil nil))
  ([reader key-fn array-coerce-fn] (parsed-seq reader key-fn array-coerce-fn nil))
  ([^BufferedReader reader key-fn array-coerce-fn field-predicate]
     (when reader
       (parsed-seq* (.createJsonParser ^JsonFactory
                                       (or factory/*json-factory*
                                           factory/json-factory) reader)
                    key-fn array-coerce-fn field-predicate))))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([reader] (parsed-smile-seq reader nil nil))
  ([reader key-fn] (parsed-smile-seq reader key-fn nil))
  ([reader key-fn array-coerce-fn] (parsed-smile-seq reader key-fn array-coerce-fn nil))
  ([^BufferedReader reader key-fn array-coerce-fn field-predicate]
     (when reader
       (parsed-seq* (.createJsonParser ^SmileFactory
                                       (or factory/*smile-factory*
                                           factory/smile-factory) reader)
                    key-fn array-coerce-fn field-predicate))))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate-stream)
(def encode-smile generate-smile)
(def decode parse-string)
(def decode-strict parse-string-strict)
(def decode-stream parse-stream)
(def decode-smile parse-smile)
