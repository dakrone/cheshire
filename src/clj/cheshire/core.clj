(ns cheshire.core
  (:import (cheshire JsonExt)
           (org.codehaus.jackson.smile SmileFactory)
           (org.codehaus.jackson JsonFactory JsonParser JsonParser$Feature)
           (java.io StringWriter StringReader BufferedReader
                    ByteArrayOutputStream))
  (:use (clojure.contrib [def :only (defvar-)])))

(def default-date-format "yyyy-MM-dd'T'HH:mm:ss'Z'")

(defvar- #^JsonFactory factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

;; Left blank here in case future options are needed
(defvar- #^SmileFactory smile-factory
  (SmileFactory.))

(defn generate-string
  {:tag String
   :doc "Returns a JSON-encoding String for the given Clojure object."}
  [obj & [date-format]]
  (let [sw        (StringWriter.)
        generator (.createJsonGenerator factory sw)]
    (JsonExt/generate generator obj (or date-format default-date-format))
    (.flush generator)
    (.toString sw)))

(defn generate-stream
  {:tag String
   :doc "Returns a BufferedWriter for the given Clojure object with the.
  JSON-encoded data written to the writer"}
  [obj #^BufferedWriter writer & [date-format]]
  (let [generator (.createJsonGenerator factory writer)]
    (JsonExt/generate generator obj (or date-format default-date-format))
    (.flush generator)
    writer))

;; alias for clojure-json users
(def encode generate-string)

(defn generate-smile
  "Returns a SMILE-encoded byte-array for the given Clojure object."
  [obj & [date-format]]
  (let [baos (ByteArrayOutputStream.)
        generator (.createJsonGenerator smile-factory baos)]
    (JsonExt/generate generator obj (or date-format default-date-format))
    (.flush generator)
    (.toByteArray baos)))

(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string."
  [string & [keywords]]
  (JsonExt/parse
   (.createJsonParser factory (StringReader. string))
   true (or keywords false) nil))

(defn parse-stream
  "Returns the Clojure object corresponding to the given reader, reader must
  implement BufferedReader."
  [#^BufferedReader rdr & [keywords]]
  (JsonExt/parse
   (.createJsonParser factory rdr)
   true (or keywords false) nil))

;; alias for clojure-json users
(def decode parse-string)

(defn parse-smile
  "Returns the Clojure object corresponding to the given SMILE-encoded bytes."
  [bytes & [keywords]]
  (JsonExt/parse
   (.createJsonParser smile-factory bytes)
   true (or keywords false) nil))

(defn- parsed-seq* [#^JsonParser parser keywords]
  (let [eof (Object.)]
    (lazy-seq
     (let [elem (JsonExt/parse parser true keywords eof)]
       (if-not (identical? elem eof)
         (cons elem (parsed-seq* parser keywords)))))))

(defn parsed-seq [#^BufferedReader reader & [keywords]]
  "Returns a lazy seq of Clojure objects corresponding to the JSON read from
  the given reader. The seq continues until the end of the reader is reached."
  (parsed-seq* (.createJsonParser factory reader) (or keywords false)))

(defn parsed-smile-seq [#^BufferedReader reader & [keywords]]
  "Returns a lazy seq of Clojure objects corresponding to the SMILE read from
  the given reader. The seq continues until the end of the reader is reached."
  (parsed-seq* (.createJsonParser smile-factory reader) (or keywords false)))
