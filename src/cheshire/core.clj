(ns cheshire.core
  "Main encoding and decoding namespace."
  (:require [cheshire.factory :as factory]
            [cheshire.generate :as gen]
            [cheshire.generate-seq :as gen-seq]
            [cheshire.parse :as parse])
  (:import (com.fasterxml.jackson.core JsonParser JsonFactory
                                       JsonGenerator PrettyPrinter
                                       JsonGenerator$Feature)
           (com.fasterxml.jackson.dataformat.cbor CBORFactory)
           (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (cheshire.prettyprint CustomPrettyPrinter)
           (java.io StringWriter StringReader BufferedReader BufferedWriter
                    ByteArrayOutputStream OutputStream Reader Writer)))

(defonce default-pretty-print-options
  {:indentation "  "
   :line-break "\n"
   :indent-arrays? false
   :indent-objects? true
   :before-array-values nil
   :after-array-values nil
   :object-field-value-separator nil})

(defn create-pretty-printer
  "Returns an instance of CustomPrettyPrinter based on the configuration
  provided as argument"
  [options]
  (let [effective-opts (merge default-pretty-print-options options)
        indentation (:indentation effective-opts)
        line-break (:line-break effective-opts)
        indent-arrays? (:indent-arrays? effective-opts)
        indent-objects? (:indent-objects? effective-opts)
        before-array-values (:before-array-values effective-opts)
        after-array-values (:after-array-values effective-opts)
        object-field-value-separator (:object-field-value-separator effective-opts)
        indent-with (condp instance? indentation
                      String indentation
                      Long (apply str (repeat indentation " "))
                      Integer (apply str (repeat indentation " "))
                      "  ")]
    (-> (new CustomPrettyPrinter)
        (.setIndentation indent-with line-break indent-objects? indent-arrays?)
        (.setBeforeArrayValues before-array-values)
        (.setAfterArrayValues after-array-values)
        (.setObjectFieldValueSeparator object-field-value-separator))))

;; Generators
(defn generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  (^String [obj]
   (generate-string obj nil))
  (^String [obj opt-map]
   (let [sw (StringWriter.)
         generator (.createGenerator
                    ^JsonFactory (or factory/*json-factory*
                                     factory/json-factory)
                    ^Writer sw)
         print-pretty (:pretty opt-map)]
     (when print-pretty
       (condp instance? print-pretty
         Boolean
           (.useDefaultPrettyPrinter generator)
         clojure.lang.IPersistentMap
           (.setPrettyPrinter generator (create-pretty-printer print-pretty))
         PrettyPrinter
           (.setPrettyPrinter generator print-pretty)
         nil))
     (when (:escape-non-ascii opt-map)
       (.enable generator JsonGenerator$Feature/ESCAPE_NON_ASCII))
     (gen/generate generator obj
                   (or (:date-format opt-map) factory/default-date-format)
                   (:ex opt-map)
                   (:key-fn opt-map))
     (.flush generator)
     (.toString sw))))

(defn generate-stream
  "Returns a BufferedWriter for the given Clojure object with the JSON-encoded
  data written to the writer. Takes an optional date format string that Date
  objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  (^BufferedWriter [obj ^BufferedWriter writer]
   (generate-stream obj writer nil))
  (^BufferedWriter [obj ^BufferedWriter writer opt-map]
   (let [generator (.createGenerator
                    ^JsonFactory (or factory/*json-factory*
                                     factory/json-factory)
                    ^Writer writer)
         print-pretty (:pretty opt-map)]
     (when print-pretty
       (condp instance? print-pretty
         Boolean
       (.useDefaultPrettyPrinter generator)
         clojure.lang.IPersistentMap
       (.setPrettyPrinter generator (create-pretty-printer print-pretty))
         PrettyPrinter
       (.setPrettyPrinter generator print-pretty)
         nil))
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
   Expects its body as sequence of write calls.
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
  - :start-inner - write object and its inner object with start border only
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
  (^bytes [obj]
   (generate-smile obj nil))
  (^bytes [obj opt-map]
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
  (^bytes [obj]
   (generate-cbor obj nil))
  (^bytes [obj opt-map]
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
  and returning the collection to be used for array values.

  If the top-level object is an array, it will be parsed lazily (use
  `parse-strict' if strict parsing is required for top-level arrays."
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

  If the top-level object is an array, it will be parsed lazily (use
  `parse-strict' if strict parsing is required for top-level arrays.

  If multiple objects (enclosed in a top-level `{}' need to be parsed lazily,
  see parsed-seq."
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
(defmacro copy-arglists
  [dst src]
  `(alter-meta! (var ~dst) merge (select-keys (meta (var ~src)) [:arglists])))
(def encode "Alias to generate-string for clojure-json users" generate-string)
(copy-arglists encode generate-string)
(def encode-stream "Alias to generate-stream for clojure-json users" generate-stream)
(copy-arglists encode-stream generate-stream)
(def encode-smile "Alias to generate-smile for clojure-json users" generate-smile)
(copy-arglists encode-smile generate-smile)
(def decode "Alias to parse-string for clojure-json users" parse-string)
(copy-arglists decode parse-string)
(def decode-strict "Alias to parse-string-strict for clojure-json users" parse-string-strict)
(copy-arglists decode-strict parse-string-strict)
(def decode-stream "Alias to parse-stream for clojure-json users" parse-stream)
(copy-arglists decode-stream parse-stream)
(def decode-smile "Alias to parse-smile for clojure-json users" parse-smile)
(copy-arglists decode-smile parse-smile)
