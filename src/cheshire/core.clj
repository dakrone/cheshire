(ns cheshire.core
  "Main encoding and decoding namespace."
  (:require [cheshire.factory :as factory]
            [cheshire.generate :as gen]
            [cheshire.generate-seq :as gen-seq]
            [cheshire.parse :as parse])
  (:import (com.fasterxml.jackson.core JsonParser JsonFactory
                                       JsonGenerator
                                       JsonGenerator$Feature)
           (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (java.io StringWriter StringReader BufferedReader BufferedWriter
                    ByteArrayOutputStream OutputStream Reader Writer)))

;; Generators
(defn ^String generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj]
   (generate-string obj nil))
  ([obj opt-map]
   (let [sw (StringWriter.)
         generator (.createGenerator
                    ^JsonFactory (or factory/*json-factory*
                                     factory/json-factory)
                    ^Writer sw)]
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
   (let [generator (.createGenerator
                    ^JsonFactory (or factory/*json-factory*
                                     factory/json-factory)
                    ^Writer writer)]
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

(defn create-generator [writer]
  "Returns JsonGenerator for given writer."
  (.createGenerator
   ^JsonFactory (or factory/*json-factory*
                    factory/json-factory)
   ^Writer writer))

(def ^:dynamic ^JsonGenerator *generator*)
(def ^:dynamic *opt-map*)

(defmacro with-writer [[writer opt-map] & body]
  "Start writing for series objects using the same json generator.
   Takes writer and options map as arguments.
   Expects it's body as sequence of write calls.
   Returns a given writer."
  `(let [c-wr# ~writer]
     (binding [*generator* (create-generator c-wr#)
               *opt-map* ~opt-map]
       ~@body
       (.flush *generator*)
       c-wr#)))

(defn write
  "Write given Clojure object as a piece of data within with-writer.
  List of wholeness acceptable values:
  - no value - the same as :all
  - :all - write object in a regular way with start and end borders
  - :start - write object with start border only
  - :start-inner - write object and it's inner object with start border only
  - :end - write object with end border only."
  ([obj] (write obj nil))
  ([obj wholeness]
   (gen-seq/generate *generator* obj (or (:date-format *opt-map*)
                                         factory/default-date-format)
                     (:ex *opt-map*)
                     (:key-fn *opt-map*)
                     :wholeness wholeness)))

(defn generate-smile
  "Returns a SMILE-encoded byte-array for the given Clojure object.
  Takes an optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj]
   (generate-smile obj nil))
  ([obj opt-map]
   (let [baos (ByteArrayOutputStream.)
         generator (.createGenerator ^SmileFactory
                                     (or factory/*smile-factory*
                                         factory/smile-factory)
                                     ^OutputStream baos)]
     (gen/generate generator obj (or (:date-format opt-map)
                                     factory/default-date-format)
                   (:ex opt-map)
                   (:key-fn opt-map))
     (.flush generator)
     (.toByteArray baos))))

(defn generate-cbor
  "Returns a CBOR-encoded byte-array for the given Clojure object.
  Takes an optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  ([obj]
   (generate-cbor obj nil))
  ([obj opt-map]
   (let [baos (ByteArrayOutputStream.)
         generator (.createGenerator ^CBORFactory
                                     (or factory/*cbor-factory*
                                         factory/cbor-factory)
                                     ^OutputStream baos)]
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
  ([^String string key-fn array-coerce-fn]
   (when string
     (parse/parse
      (.createParser ^JsonFactory (or factory/*json-factory*
                                      factory/json-factory)
                     ^Reader (StringReader. string))
      key-fn nil array-coerce-fn))))

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
  ([^String string key-fn array-coerce-fn]
   (when string
     (parse/parse-strict
      (.createParser ^JsonFactory (or factory/*json-factory*
                                      factory/json-factory)
                     ^Writer (StringReader. string))
      key-fn nil array-coerce-fn))))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. An optional key-fn argument can be either true (to
  coerce keys to keywords),false to leave them as strings, or a function to
  provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If laziness is needed, see parsed-seq."
  ([rdr] (parse-stream rdr nil nil))
  ([rdr key-fn] (parse-stream rdr key-fn nil))
  ([^BufferedReader rdr key-fn array-coerce-fn]
   (when rdr
     (parse/parse
      (.createParser ^JsonFactory (or factory/*json-factory*
                                      factory/json-factory)
                     ^Reader rdr)
      key-fn nil array-coerce-fn))))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([bytes] (parse-smile bytes nil nil))
  ([bytes key-fn] (parse-smile bytes key-fn nil))
  ([^bytes bytes key-fn array-coerce-fn]
   (when bytes
     (parse/parse
      (.createParser ^SmileFactory (or factory/*smile-factory*
                                       factory/smile-factory) bytes)
      key-fn nil array-coerce-fn))))

(defn parse-cbor
  "Returns the Clojure object corresponding to the given CBOR-encoded bytes.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([bytes] (parse-cbor bytes nil nil))
  ([bytes key-fn] (parse-cbor bytes key-fn nil))
  ([^bytes bytes key-fn array-coerce-fn]
   (when bytes
     (parse/parse
      (.createParser ^CBORFactory (or factory/*cbor-factory*
                                      factory/cbor-factory) bytes)
      key-fn nil array-coerce-fn))))

(def ^{:doc "Object used to determine end of lazy parsing attempt."}
  eof (Object.))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser key-fn array-coerce-fn]
  (lazy-seq
   (let [elem (parse/parse-strict parser key-fn eof array-coerce-fn)]
     (when-not (identical? elem eof)
       (cons elem (parsed-seq* parser key-fn array-coerce-fn))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If non-laziness is needed, see parse-stream."
  ([reader] (parsed-seq reader nil nil))
  ([reader key-fn] (parsed-seq reader key-fn nil))
  ([^BufferedReader reader key-fn array-coerce-fn]
   (when reader
     (parsed-seq* (.createParser ^JsonFactory
                                 (or factory/*json-factory*
                                     factory/json-factory)
                                 ^Reader reader)
                  key-fn array-coerce-fn))))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  ([reader] (parsed-smile-seq reader nil nil))
  ([reader key-fn] (parsed-smile-seq reader key-fn nil))
  ([^BufferedReader reader key-fn array-coerce-fn]
   (when reader
     (parsed-seq* (.createParser ^SmileFactory
                                 (or factory/*smile-factory*
                                     factory/smile-factory)
                                 ^Reader reader)
                  key-fn array-coerce-fn))))

;; aliases for clojure-json users
(def encode "Alias to generate-string for clojure-json users" generate-string)
(def encode-stream "Alias to generate-stream for clojure-json users" generate-stream)
(def encode-smile "Alias to generate-smile for clojure-json users" generate-smile)
(def decode "Alias to parse-string for clojure-json users" parse-string)
(def decode-strict "Alias to parse-string-strict for clojure-json users" parse-string-strict)
(def decode-stream "Alias to parse-stream for clojure-json users" parse-stream)
(def decode-smile "Alias to parse-smile for clojure-json users" parse-smile)
