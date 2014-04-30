(ns cheshire.parse
  (:import (com.fasterxml.jackson.core JsonParser JsonToken)))

(declare parse*)

(def ^{:doc "Flag to determine whether float values should be returned as
             BigDecimals to retain precision. Defaults to false."
       :dynamic true}
  *use-bigdecimals?* false)

(defmacro ^:private tag
  ([obj]
     `(vary-meta ~obj assoc :tag `JsonParser)))

(definline parse-object
  [^JsonParser jp key-fn parent-field field-predicate bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 mmap# (if (or (nil? ~field-predicate)
                             (~field-predicate ~parent-field key-str#))
                         (assoc! mmap# (~key-fn key-str#)
                           (parse* ~jp ~key-fn key-str# ~field-predicate ~bd? ~array-coerce-fn))
                         (do
                           (.skipChildren ~jp)
                           mmap#))]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-array [^JsonParser jp key-fn parent-field field-predicate bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient (if ~array-coerce-fn
                                 (~array-coerce-fn array-field-name#)
                                 []))]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                         (parse* ~jp ~key-fn ~parent-field ~field-predicate ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(defn lazily-parse-array [^JsonParser jp key-fn field-predicate bd? array-coerce-fn]
  (lazy-seq
   (loop [chunk-idx 0, buf (chunk-buffer 32)]
     (if (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
       (chunk-cons (chunk buf) nil)
       (do
         (chunk-append buf (parse* jp key-fn nil field-predicate bd? array-coerce-fn))
         (.nextToken jp)
         (let [chunk-idx* (unchecked-inc chunk-idx)]
           (if (< chunk-idx* 32)
             (recur chunk-idx* buf)
             (chunk-cons
              (chunk buf)
              (lazily-parse-array jp key-fn field-predicate bd? array-coerce-fn)))))))))

(defn parse* [^JsonParser jp key-fn parent-field field-predicate bd? array-coerce-fn]
  (condp identical? (.getCurrentToken jp)
    JsonToken/START_OBJECT (parse-object jp key-fn parent-field field-predicate bd? array-coerce-fn)
    JsonToken/START_ARRAY (parse-array jp key-fn parent-field field-predicate bd? array-coerce-fn)
    JsonToken/VALUE_STRING (.getText jp)
    JsonToken/VALUE_NUMBER_INT (.getNumberValue jp)
    JsonToken/VALUE_NUMBER_FLOAT (if bd?
                                   (.getDecimalValue jp)
                                   (.getNumberValue jp))
    JsonToken/VALUE_TRUE true
    JsonToken/VALUE_FALSE false
    JsonToken/VALUE_NULL nil
    (throw
     (Exception.
       (str "Cannot parse " (pr-str (.getCurrentToken jp)))))))

(defn parse-strict [^JsonParser jp key-fn eof array-coerce-fn field-predicate]
  (let [key-fn (or (if (identical? key-fn true) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof
      (parse* jp key-fn nil field-predicate *use-bigdecimals?* array-coerce-fn))))

(defn parse [^JsonParser jp key-fn eof array-coerce-fn field-predicate]
  (let [key-fn (or (if (true? key-fn) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof

      JsonToken/START_ARRAY
      (do
        (.nextToken jp)
        (lazily-parse-array jp key-fn field-predicate *use-bigdecimals?* array-coerce-fn))

      (parse* jp key-fn nil field-predicate *use-bigdecimals?* array-coerce-fn))))
