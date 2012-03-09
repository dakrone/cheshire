(ns cheshire.custom
  "Methods used for extending JSON generation to different Java classes.
  Has the same public API as core.clj so they can be swapped in and out."
  (:use [cheshire.factory])
  (:require [cheshire.core :as core])
  (:import (java.io BufferedWriter ByteArrayOutputStream StringWriter)
           (java.util Date SimpleTimeZone)
           (java.text SimpleDateFormat)
           (java.sql Timestamp)
           (org.codehaus.jackson.smile SmileFactory)
           (org.codehaus.jackson JsonFactory JsonGenerator
                                 JsonGenerationException JsonParser)))

;; date format rebound for custom encoding
(def ^{:dynamic true :private true} *date-format*)

(defprotocol JSONable
  (to-json [t jg]))

(defn ^String encode* [obj & [^String date-format]]
  (binding [*date-format* (or date-format default-date-format)]
    (let [sw (StringWriter.)
          generator (.createJsonGenerator
                     ^JsonFactory (or *json-factory* json-factory) sw)]
      (if obj
        (to-json obj generator)
        (.writeNull generator))
      (.flush generator)
      (.toString sw))))

(defn ^String encode [obj & [^String date-format]]
  (try
    (core/encode obj date-format)
    (catch JsonGenerationException _
      (encode* obj date-format))))

(defn ^String encode-stream* [obj ^BufferedWriter w & [^String date-format]]
  (binding [*date-format* (or date-format default-date-format)]
    (let [generator (.createJsonGenerator ^JsonFactory
                                          (or *json-factory* json-factory) w)]
      (to-json obj generator)
      (.flush generator)
      w)))

(defn ^String encode-stream [obj ^BufferedWriter w & [^String date-format]]
  (try
    (core/encode-stream obj w date-format)
    (catch JsonGenerationException _
      (encode-stream* obj date-format))))

(defn encode-smile*
  [obj & [^String date-format]]
  (binding [*date-format* (or date-format default-date-format)]
    (let [baos (ByteArrayOutputStream.)
          generator (.createJsonGenerator ^SmileFactory
                                          (or *smile-factory* smile-factory)
                                          baos)]
      (to-json obj generator)
      (.flush generator)
      (.toByteArray baos))))

(defn encode-smile
  [obj & [^String date-format]]
  (try
    (core/encode-smile obj date-format)
    (catch JsonGenerationException _
      (encode-smile* obj date-format))))

;; there are no differences in parsing, but these are here to make
;; this a self-contained namespace if desired
(def parse core/decode)
(def parse-string core/decode)
(def parse-stream core/decode-stream)
(def parse-smile core/decode-smile)
(def parsed-seq core/parsed-seq)
(def decode core/parse-string)
(def decode-stream parse-stream)
(def decode-smile parse-smile)

;; aliases for encoding
(def generate-string encode)
(def generate-string* encode*)
(def generate-stream encode-stream)
(def generate-stream* encode-stream*)
(def generate-smile encode-smile)
(def generate-smile* encode-smile*)

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
  (.writeNumber jg n))

(defn encode-long
  "Encode anything implementing java.lang.Number to the json generator."
  [^Long n ^JsonGenerator jg]
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
                          (name k)
                          (str k)))
    (to-json v jg))
  (.writeEndObject jg))

(defn encode-symbol
  "Encode a clojure symbol to the json generator."
  [^clojure.lang.Symbol s ^JsonGenerator jg]
  (.writeString jg (str s)))

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

(defn remove-encoder [cls]
  "Remove encoder for a given type.

   ex. (remove-encoder java.net.URL)"
  (alter-var-root #'JSONable #(assoc % :impls (dissoc (:impls %) cls)))
  (clojure.core/-reset-methods JSONable))
