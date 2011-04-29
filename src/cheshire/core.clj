(ns cheshire.core
  (:use [cheshire.parse :only [parse]]
        [cheshire.generate :only [generate]])
  (:import (org.codehaus.jackson.smile SmileFactory)
           (org.codehaus.jackson JsonFactory JsonParser JsonParser$Feature
                                 JsonGenerator)
           (java.io StringWriter StringReader BufferedReader BufferedWriter
                    ByteArrayOutputStream)))

;; default date format used to JSON-encode Date objects
(def default-date-format "yyyy-MM-dd'T'HH:mm:ss'Z'")

;; Factory objects that are needed to do the encoding and decoding
(def ^{:private true :tag JsonFactory} factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

(def ^{:private true :tag SmileFactory} smile-factory
  (SmileFactory.))

;; Generators
(defn ^String generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj & [date-format]]
  (let [sw (StringWriter.)
        generator (.createJsonGenerator factory sw)]
    (generate generator obj (or date-format default-date-format))
    (.flush generator)
    (.toString sw)))

(defn ^String generate-stream
  "Returns a BufferedWriter for the given Clojure object with the.
  JSON-encoded data written to the writer. Takes an optional date
  format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj ^BufferedWriter writer & [^String date-format]]
  (let [generator (.createJsonGenerator factory writer)]
    (generate generator obj (or date-format default-date-format))
    (.flush generator)
    writer))

(defn generate-smile
  "Returns a SMILE-encoded byte-array for the given Clojure object.
  Takes an optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj & [^String date-format]]
  (let [baos (ByteArrayOutputStream.)
        generator (.createJsonGenerator smile-factory baos)]
    (generate generator obj (or date-format default-date-format))
    (.flush generator)
    (.toByteArray baos)))

;; Parsers
(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords."
  [^String string & [keywords?]]
  (parse
   (.createJsonParser factory (StringReader. string))
   true (or keywords? false) nil))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. keywords? should be true if keyword keys are needed
  the default is false, maps will use strings as keywords.

  If laziness is needed, see parsed-seq."
  [^BufferedReader rdr & [keywords?]]
  (parse
   (.createJsonParser factory rdr)
   true (or keywords? false) nil))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords."
  [^bytes bytes & [keywords?]]
  (parse
   (.createJsonParser smile-factory bytes)
   true (or keywords? false) nil))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser keywords?]
  (let [eof (Object.)]
    (lazy-seq
     (let [elem (parse parser true keywords? eof)]
       (if-not (identical? elem eof)
         (cons elem (parsed-seq* parser keywords?)))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  If non-laziness is needed, see parse-stream."
  [^BufferedReader reader & [keywords?]]
  (parsed-seq* (.createJsonParser factory reader) (or keywords? false)))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached."
  [^BufferedReader reader & [keywords?]]
  (parsed-seq* (.createJsonParser smile-factory reader) (or keywords? false)))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate-stream)
(def encode-smile generate-smile)
(def decode parse-string)
(def decode-stream parse-stream)
(def decode-smile parse-smile)
