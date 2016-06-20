(ns cheshire.custom
  "DEPRECATED

  Methods used for extending JSON generation to different Java classes.
  Has the same public API as core.clj so they can be swapped in and out."
  (:use [cheshire.factory])
  (:require [cheshire.core :as core]
            [cheshire.generate :as generate])
  (:import (java.io BufferedWriter ByteArrayOutputStream StringWriter)
           (java.util Date SimpleTimeZone)
           (java.text SimpleDateFormat)
           (java.sql Timestamp)
           (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (com.fasterxml.jackson.core JsonFactory JsonGenerator
                                       JsonGenerator$Feature
                                       JsonGenerationException JsonParser)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;  DEPRECATED, DO NOT USE  ;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; date format rebound for custom encoding
(def ^{:dynamic true :private true} *date-format*)

;; pre-allocated exception for fast-failing core attempt for custom encoding
(def ^{:private true} core-failure (JsonGenerationException.
                                    "Cannot custom JSON encode object"))

(defprotocol JSONable
  (to-json [t jg]))

(defn encode*
  (^String [obj]
     (encode* obj nil))
  (^String [obj opt-map]
     (binding [*date-format* (or (:date-format opt-map) default-date-format)]
       (let [sw (StringWriter.)
             generator (.createJsonGenerator
                        ^JsonFactory (or *json-factory* json-factory) sw)]
         (when (:pretty opt-map)
           (.useDefaultPrettyPrinter generator))
         (when (:escape-non-ascii opt-map)
           (.enable generator JsonGenerator$Feature/ESCAPE_NON_ASCII))
         (if obj
           (to-json obj generator)
           (.writeNull generator))
         (.flush generator)
         (.toString sw)))))

(def encode encode*)
(core/copy-arglists encode encode*)

(defn encode-stream*
  (^String [obj ^BufferedWriter w]
     (encode-stream* obj w nil))
  (^String [obj ^BufferedWriter w opt-map]
     (binding [*date-format* (or (:date-format opt-map) default-date-format)]
       (let [generator (.createJsonGenerator
                        ^JsonFactory (or *json-factory* json-factory) w)]
         (when (:pretty opt-map)
           (.useDefaultPrettyPrinter generator))
         (when (:escape-non-ascii opt-map)
           (.enable generator JsonGenerator$Feature/ESCAPE_NON_ASCII))
         (to-json obj generator)
         (.flush generator)
         w))))

(def encode-stream encode-stream*)
(core/copy-arglists encode-stream encode-stream*)

(defn encode-smile*
  (^bytes [obj]
     (encode-smile* obj nil))
  (^bytes [obj opt-map]
     (binding [*date-format* (or (:date-format opt-map) default-date-format)]
       (let [baos (ByteArrayOutputStream.)
             generator (.createJsonGenerator ^SmileFactory
                                             (or *smile-factory* smile-factory)
                                             baos)]
         (to-json obj generator)
         (.flush generator)
         (.toByteArray baos)))))

(def encode-smile encode-smile*)
(core/copy-arglists encode-smile encode-smile*)

;; there are no differences in parsing, but these are here to make
;; this a self-contained namespace if desired
(def parse core/decode)
(core/copy-arglists parse core/decode)
(def parse-string core/decode)
(core/copy-arglists parse-string core/decode)
(def parse-stream core/decode-stream)
(core/copy-arglists parse-stream core/decode-stream)
(def parse-smile core/decode-smile)
(core/copy-arglists parse-smile core/decode-smile)
(def parsed-seq core/parsed-seq)
(core/copy-arglists parsed-seq core/parsed-seq)
(def decode core/parse-string)
(core/copy-arglists decode core/parse-string)
(def decode-stream parse-stream)
(core/copy-arglists decode-stream core/parse-stream)
(def decode-smile parse-smile)
(core/copy-arglists decode-smile core/parse-smile)

;; aliases for encoding
(def generate-string encode*)
(core/copy-arglists generate-string encode*)
(def generate-string* encode*)
(core/copy-arglists generate-string* encode*)
(def generate-stream encode-stream*)
(core/copy-arglists generate-stream encode-stream*)
(def generate-stream* encode-stream*)
(core/copy-arglists generate-stream* encode-stream*)
(def generate-smile encode-smile*)
(core/copy-arglists generate-smile encode-smile*)
(def generate-smile* encode-smile*)
(core/copy-arglists generate-smile* encode-smile*)

;; Generic encoders, these can be used by someone writing a custom
;; encoder if so desired, after transforming an arbitrary data
;; structure into a clojure one, these can just be called.
(defn encode-nil
  "Encode null to the json generator."
  [_ ^JsonGenerator jg]
  (.writeNull jg))

(defn encode-str
  "Encode a string to the json generator."
  [^String s ^JsonGenerator jg]
  (.writeString jg (str s)))

(defn encode-number
  "Encode anything implementing java.lang.Number to the json generator."
  [^java.lang.Number n ^JsonGenerator jg]
  (generate/encode-number n jg))

(defn encode-long
  "Encode anything implementing java.lang.Number to the json generator."
  [^Long n ^JsonGenerator jg]
  (.writeNumber jg (long n)))

(defn encode-int
  "Encode anything implementing java.lang.Number to the json generator."
  [n ^JsonGenerator jg]
  (.writeNumber jg (long n)))

(defn encode-ratio
  "Encode a clojure.lang.Ratio to the json generator."
  [^clojure.lang.Ratio n ^JsonGenerator jg]
  (.writeNumber jg (double n)))

(defn encode-seq
  "Encode a seq to the json generator."
  [s ^JsonGenerator jg]
  (.writeStartArray jg)
  (doseq [i s]
    (to-json i jg))
  (.writeEndArray jg))

(defn encode-date
  "Encode a date object to the json generator."
  [^Date d ^JsonGenerator jg]
  (let [sdf (SimpleDateFormat. *date-format*)]
    (.setTimeZone sdf (SimpleTimeZone. 0 "UTC"))
    (.writeString jg (.format sdf d))))

(defn encode-bool
  "Encode a Boolean object to the json generator."
  [^Boolean b ^JsonGenerator jg]
  (.writeBoolean jg b))

(defn encode-named
  "Encode a keyword to the json generator."
  [^clojure.lang.Keyword k ^JsonGenerator jg]
  (.writeString jg (if-let [ns (namespace k)]
                     (str ns "/" (name k))
                     (name k))))

(defn encode-map
  "Encode a clojure map to the json generator."
  [^clojure.lang.IPersistentMap m ^JsonGenerator jg]
  (.writeStartObject jg)
  (doseq [[k v] m]
    (.writeFieldName jg (if (instance? clojure.lang.Keyword k)
                          (if-let [ns (namespace k)]
                            (str ns "/" (name k))
                            (name k))
                          (str k)))
    (to-json v jg))
  (.writeEndObject jg))

(defn encode-symbol
  "Encode a clojure symbol to the json generator."
  [^clojure.lang.Symbol s ^JsonGenerator jg]
  (.writeString jg (str s)))

;; extended implementations for clojure datastructures
(extend nil
  JSONable
  {:to-json encode-nil})

(extend java.lang.String
  JSONable
  {:to-json encode-str})

;; This is lame, thanks for changing all the BigIntegers to BigInts
;; in 1.3 clojure/core :-/
(defmacro handle-bigint []
  (when (not= {:major 1 :minor 2} (select-keys *clojure-version*
                                               [:major :minor]))
    `(extend clojure.lang.BigInt
       JSONable
       {:to-json ~'(fn encode-bigint
                     [^clojure.lang.BigInt n ^JsonGenerator jg]
                     (.writeNumber jg (.toBigInteger n)))})))
(handle-bigint)

(extend clojure.lang.Ratio
  JSONable
  {:to-json encode-ratio})

(extend Long
  JSONable
  {:to-json encode-long})

(extend Short
  JSONable
  {:to-json encode-int})

(extend Byte
  JSONable
  {:to-json encode-int})

(extend java.lang.Number
  JSONable
  {:to-json encode-number})

(extend clojure.lang.ISeq
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentVector
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentSet
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentList
  JSONable
  {:to-json encode-seq})

(extend java.util.Date
  JSONable
  {:to-json encode-date})

(extend java.sql.Timestamp
  JSONable
  {:to-json #(encode-date (Date. (.getTime ^java.sql.Timestamp %1)) %2)})

(extend java.util.UUID
  JSONable
  {:to-json encode-str})

(extend java.lang.Boolean
  JSONable
  {:to-json encode-bool})

(extend clojure.lang.Keyword
  JSONable
  {:to-json encode-named})

(extend clojure.lang.IPersistentMap
  JSONable
  {:to-json encode-map})

(extend clojure.lang.Symbol
  JSONable
  {:to-json encode-symbol})

(extend clojure.lang.Associative
  JSONable
  {:to-json encode-map})

(extend java.util.Map
  JSONable
  {:to-json encode-map})

(extend java.util.Set
  JSONable
  {:to-json encode-seq})

(extend java.util.List
  JSONable
  {:to-json encode-seq})
;; Utility methods to add and remove encoders
(defn add-encoder
  "Provide an encoder for a type not handled by Cheshire.

   ex. (add-encoder java.net.URL encode-string)

   See encode-str, encode-map, etc, in the cheshire.custom
   namespace for encoder examples."
  [cls encoder]
  (extend cls
    JSONable
    {:to-json encoder}))

(defn remove-encoder
  "Remove encoder for a given type.

   ex. (remove-encoder java.net.URL)"
  [cls]
  (alter-var-root #'JSONable #(assoc % :impls (dissoc (:impls %) cls)))
  (clojure.core/-reset-methods JSONable))
