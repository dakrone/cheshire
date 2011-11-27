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
(defn- default-array-coerce-fn [_] [])

(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords.
  The array-coerce-fn is an optional function taking the name of an array field, 
  and returning the collection to be used for array values."
  [^String string & [^Boolean keywords? array-coerce-fn]]
  (when string
    (parse
     (.createJsonParser factory (StringReader. string))
     true (or keywords? false) nil
     (or array-coerce-fn default-array-coerce-fn))))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader. keywords? should be true if keyword keys are needed
  the default is false, maps will use strings as keywords.
  The array-coerce-fn is an optional function taking the name of an array field, 
  and returning the collection to be used for array values.
  If laziness is needed, see parsed-seq."
  [^BufferedReader rdr & [^Boolean keywords? array-coerce-fn] ]
  (when rdr
    (parse
     (.createJsonParser factory rdr)
     true (or keywords? false) nil
     (or array-coerce-fn default-array-coerce-fn))))

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes.
  keywords? should be true if keyword keys are needed, the default is false
  maps will use strings as keywords.
  The array-coerce-fn is an optional function taking the name of an array field, 
  and returning the collection to be used for array values."
  [^bytes bytes & [^Boolean keywords? array-coerce-fn]]
  (when bytes
    (parse
     (.createJsonParser smile-factory bytes)
     true (or keywords? false) nil
     (or array-coerce-fn default-array-coerce-fn))))

;; Lazy parsers
(defn- parsed-seq*
  "Internal lazy-seq parser"
  [^JsonParser parser ^Boolean keywords? array-coerce-fn]
  (let [eof (Object.)]
    (lazy-seq
     (let [elem (parse parser true keywords? eof array-coerce-fn)]
       (if-not (identical? elem eof)
         (cons elem (parsed-seq* parser keywords? array-coerce-fn)))))))

(defn parsed-seq
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached.
  The array-coerce-fn is an optional function taking the name of an array field, 
  and returning the collection to be used for array values.
  If non-laziness is needed, see parse-stream."
  [^BufferedReader reader & [^Boolean keywords? array-coerce-fn]]
  (when reader
    (parsed-seq* (.createJsonParser factory reader) (or keywords? false) (or array-coerce-fn default-array-coerce-fn))))

(defn parsed-smile-seq
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached.
  The array-coerce-fn is an optional function taking the name of an array field, 
  and returning the collection to be used for array values."
  [^BufferedReader reader & [^Boolean keywords? array-coerce-fn]]
  (when reader
    (parsed-seq* (.createJsonParser smile-factory reader) (or keywords? false) (or array-coerce-fn default-array-coerce-fn))))

;; aliases for clojure-json users
(def encode generate-string)
(def encode-stream generate-stream)
(def encode-smile generate-smile)
(def decode parse-string)
(def decode-stream parse-stream)
(def decode-smile parse-smile)
