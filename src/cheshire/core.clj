(ns cheshire.core
  (:use [cheshire.factory]
        [cheshire.generate :only [generate]]
        [cheshire.parse :only [parse]])
  (:import (org.codehaus.jackson JsonParser)
           (java.io StringWriter StringReader BufferedReader BufferedWriter
                    ByteArrayOutputStream)))

;; Generators
(defn ^String generate-string
  "Returns a JSON-encoding String for the given Clojure object. Takes an
  optional date format string that Date objects will be encoded with.

  The default date format (in UTC) is: yyyy-MM-dd'T'HH:mm:ss'Z'"
  [obj & [^String date-format]]
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
  [^String string & [^Boolean keywords?]]
  (when string
    (parse
     (.createJsonParser factory (StringReader. string))
     true (or keywords? false) nil)))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. keywords? should be true if keyword keys are needed
  the default is false, maps will use strings as keywords.

  If laziness is needed, see parsed-seq."
  [^BufferedReader rdr & [^Boolean keywords?]]
  (when rdr
    (parse
     (.createJsonParser factory rdr)
     true (or keywords? false) nil)))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords."
  [^bytes bytes & [^Boolean keywords?]]
  (when bytes
    (parse
     (.createJsonParser smile-factory bytes)
     true (or keywords? false) nil)))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser ^Boolean keywords?]
  (let [eof (Object.)]
    (lazy-seq
     (let [elem (parse parser true keywords? eof)]
       (if-not (identical? elem eof)
         (cons elem (parsed-seq* parser keywords?)))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.

  If non-laziness is needed, see parse-stream."
  [^BufferedReader reader & [^Boolean keywords?]]
  (when reader
    (parsed-seq* (.createJsonParser factory reader) (or keywords? false))))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached."
  [^BufferedReader reader & [^Boolean keywords?]]
  (when reader
    (parsed-seq* (.createJsonParser smile-factory reader) (or keywords? false))))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate-stream)
(def encode-smile generate-smile)
(def decode parse-string)
(def decode-stream parse-stream)
(def decode-smile parse-smile)
