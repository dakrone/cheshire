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
                     (or (:key-fn opt-map) (fn [k] (.substring (str k) 1))))
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
                     (or (:key-fn opt-map) name))
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
                     (or (:key-fn opt-map) name))
       (.flush generator)
       (.toByteArray baos))))

;; Parsers
(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  [^String string & [key-fn array-coerce-fn]]
  (when string
    (parse/parse
     (.createJsonParser ^JsonFactory (or factory/*json-factory*
                                         factory/json-factory)
                        (StringReader. string))
     key-fn nil array-coerce-fn)))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. An optional key-fn argument can be either true (to
  coerce keys to keywords),false to leave them as strings, or a function to
  provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If laziness is needed, see parsed-seq."
  [^BufferedReader rdr & [key-fn array-coerce-fn]]
  (when rdr
    (parse/parse
     (.createJsonParser ^JsonFactory (or factory/*json-factory*
                                         factory/json-factory) rdr)
     key-fn nil array-coerce-fn)))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  An optional key-fn argument can be either true (to coerce keys to keywords),
  false to leave them as strings, or a function to provide custom coercion.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  [^bytes bytes & [key-fn array-coerce-fn]]
  (when bytes
    (parse/parse
     (.createJsonParser ^SmileFactory (or factory/*smile-factory*
                                          factory/smile-factory) bytes)
     key-fn nil array-coerce-fn)))

(def ^{:doc "Object used to determine end of lazy parsing attempt."}
  eof (Object.))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser key-fn array-coerce-fn]
  (lazy-seq
   (let [elem (parse/parse parser key-fn eof array-coerce-fn)]
     (when-not (identical? elem eof)
       (cons elem (parsed-seq* parser key-fn array-coerce-fn))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values.
  If non-laziness is needed, see parse-stream."
  [^BufferedReader reader & [key-fn array-coerce-fn]]
  (when reader
    (parsed-seq* (.createJsonParser ^JsonFactory
                                    (or factory/*json-factory*
                                        factory/json-factory) reader)
                 key-fn array-coerce-fn)))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached.

  The array-coerce-fn is an optional function taking the name of an array field,
  and returning the collection to be used for array values."
  [^BufferedReader reader & [key-fn array-coerce-fn]]
  (when reader
    (parsed-seq* (.createJsonParser ^SmileFactory
                                    (or factory/*smile-factory*
                                        factory/smile-factory) reader)
                 key-fn array-coerce-fn)))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate-stream)
(def encode-smile generate-smile)
(def decode parse-string)
(def decode-stream parse-stream)
(def decode-smile parse-smile)
