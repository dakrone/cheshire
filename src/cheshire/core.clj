(ns cheshire.core
  (:require [cheshire.generate]
            [cheshire.parse])
  (:import (java.io ByteArrayOutputStream StringWriter)
           (org.codehaus.jackson JsonFactory JsonGenerator JsonParser
                                 JsonParser$Feature)
           (org.codehaus.jackson.smile SmileFactory)))

;; default date format used to JSON-encode Date objects
(def default-date-format "yyyy-MM-dd'T'HH:mm:ss'Z'")

;; Factory objects that are needed to do the encoding and decoding
(def ^{:private true :tag JsonFactory} factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

(def ^{:private true :tag SmileFactory} smile-factory
  (SmileFactory.))

;; Generators
(defn ^String generate
  "Generates JSON-encoded data into the given writeable, and returns the
  writeable. writeable is anything that is accepted by Jackson's
  JsonFactory/createJsonGenerator. Takes an optional date format string
  that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj writeable & [^String date-format]]
  (let [generator (.createJsonGenerator factory writeable)]
    (cheshire.generate/generate generator obj
                                (or date-format default-date-format))
    (.flush generator)
    writeable))

(defn ^String generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj & [date-format]]
  (let [sw (StringWriter.)]
    (cheshire.generate/generate obj sw date-format)
    (.toString sw)))

(defn generate-smile
  "Returns a SMILE-encoded byte-array for the given Clojure object.
  Takes an optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj & [^String date-format]]
  (let [baos (ByteArrayOutputStream.)
        generator (.createJsonGenerator smile-factory baos)]
    (cheshire.generate/generate generator obj
                                (or date-format default-date-format))
    (.flush generator)
    (.toByteArray baos)))

;; Parsers

(defn parse
  "Returns the Clojure object corresponding to the given JSON.  parseable is
  anything that is accepted by Jackson's JsonFactory/createJSonParser.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords."
  [parseable & [keywords?]]
  (cheshire.parse/parse
   (.createJsonParser factory parseable)
   true (or keywords? false) nil))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  parseable is anything that is accepted by Jackson's
  SmileFactory/createJsonParser. keywords? should be true if keyword keys are
  needed, the default is false maps will use strings as keywords."
  [parseable & [keywords?]]
  (cheshire.parse/parse
   (.createJsonParser smile-factory parseable)
   true (or keywords? false) nil))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser keywords?]
  (let [eof (Object.)]
    (lazy-seq
     (let [elem (cheshire.parse/parse parser true keywords? eof)]
       (if-not (identical? elem eof)
         (cons elem (parsed-seq* parser keywords?)))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  If non-laziness is needed, see parse-stream."
  [parseable & [keywords?]]
  (parsed-seq* (.createJsonParser factory parseable) (or keywords? false)))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached."
  [parseable reader & [keywords?]]
  (parsed-seq* (.createJsonParser smile-factory parseable) (or keywords? false)))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate)
(def encode-smile generate-smile)
(def decode parse)
(def decode-stream parse)
(def decode-smile parse-smile)
