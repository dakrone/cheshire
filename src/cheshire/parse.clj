(ns cheshire.parse
  (:require [cheshire.factory :as factory])
  (:import (java.io DataInput File InputStream Reader)
           (java.net URL)
           (com.fasterxml.jackson.core JsonFactory JsonParser JsonToken)
           (com.fasterxml.jackson.dataformat.cbor CBORFactory)
           (com.fasterxml.jackson.dataformat.smile SmileFactory)))

(defprotocol ToJsonParser
  (-json-parser [self ^JsonFactory factory]))

(defprotocol ToCBORParser
  (-cbor-parser [self ^CBORFactory factory]))

(defprotocol ToSmileParser
  (-smile-parser [self ^SmileFactory factory]))

(defmacro extend-parser [Protocol method Factory & classes]
  (let [factory (with-meta 'factory {:tag Factory})]
    `(extend-protocol ~Protocol
       ~@(mapcat
           (fn [T] [T `(~method [~'self ~factory] ~'(.createParser factory self))])
           classes))))

(extend-type (Class/forName "[B")
  ToJsonParser
  (-json-parser [self ^JsonFactory factory] (.createParser factory self)))

(extend-type (Class/forName "[C")
  ToJsonParser
  (-json-parser [self ^JsonFactory factory] (.createParser factory self)))

(extend-parser ToJsonParser -json-parser JsonFactory
  DataInput
  File
  InputStream
  Reader
  String
  URL)

(extend-type (Class/forName "[B")
  ToCBORParser
  (-cbor-parser [self ^CBORFactory factory] (.createParser factory self)))

(extend-parser ToCBORParser -cbor-parser CBORFactory
  File
  InputStream
  URL)

(extend-type (Class/forName "[B")
  ToSmileParser
  (-smile-parser [self ^SmileFactory factory] (.createParser factory self)))

(extend-parser ToSmileParser -smile-parser SmileFactory
  File
  InputStream
  URL
  Reader) ; FIXME: Will actually parse JSON, not SMILE. Kept for backwards compatibility.

(defn json-parser [input]
  (-json-parser input (or factory/*json-factory* factory/json-factory)))

(defn cbor-parser [input]
  (-cbor-parser input (or factory/*cbor-factory* factory/cbor-factory)))

(defn smile-parser [input]
  (-smile-parser input (or factory/*smile-factory* factory/smile-factory)))

(declare parse*)

(def ^:dynamic *chunk-size* 32)

(def ^{:doc "Flag to determine whether float values should be returned as
             BigDecimals to retain precision. Defaults to false."
       :dynamic true}
  *use-bigdecimals?* false)

(defmacro ^:private tag
  ([obj]
     `(vary-meta ~obj assoc :tag `JsonParser)))

(definline parse-object [^JsonParser jp key-fn bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 key# (~key-fn key-str#)
                 mmap# (assoc! mmap# key#
                               (parse* ~jp ~key-fn ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-array [^JsonParser jp key-fn bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient (if ~array-coerce-fn
                                 (~array-coerce-fn array-field-name#)
                                 []))]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                              (parse* ~jp ~key-fn ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(defn lazily-parse-array [^JsonParser jp key-fn bd? array-coerce-fn]
  (lazy-seq
   (loop [chunk-idx 0, buf (chunk-buffer *chunk-size*)]
     (if (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
       (chunk-cons (chunk buf) nil)
       (do
         (chunk-append buf (parse* jp key-fn bd? array-coerce-fn))
         (.nextToken jp)
         (let [chunk-idx* (unchecked-inc chunk-idx)]
           (if (< chunk-idx* *chunk-size*)
             (recur chunk-idx* buf)
             (chunk-cons
              (chunk buf)
              (lazily-parse-array jp key-fn bd? array-coerce-fn)))))))))

(defn parse* [^JsonParser jp key-fn bd? array-coerce-fn]
  (condp identical? (.getCurrentToken jp)
    JsonToken/START_OBJECT (parse-object jp key-fn bd? array-coerce-fn)
    JsonToken/START_ARRAY (parse-array jp key-fn bd? array-coerce-fn)
    JsonToken/VALUE_STRING (.getText jp)
    JsonToken/VALUE_NUMBER_INT (.getNumberValue jp)
    JsonToken/VALUE_NUMBER_FLOAT (if bd?
                                   (.getDecimalValue jp)
                                   (.getNumberValue jp))
    JsonToken/VALUE_EMBEDDED_OBJECT (.getBinaryValue jp)
    JsonToken/VALUE_TRUE true
    JsonToken/VALUE_FALSE false
    JsonToken/VALUE_NULL nil
    (throw
     (Exception.
      (str "Cannot parse " (pr-str (.getCurrentToken jp)))))))

(defn parse-strict [^JsonParser jp key-fn eof array-coerce-fn]
  (let [key-fn (or (if (identical? key-fn true) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof
      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn))))

(defn parse [^JsonParser jp key-fn eof array-coerce-fn]
  (let [key-fn (or (if (and (instance? Boolean key-fn) key-fn) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof

      JsonToken/START_ARRAY
      (do
        (.nextToken jp)
        (lazily-parse-array jp key-fn *use-bigdecimals?* array-coerce-fn))

      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn))))
