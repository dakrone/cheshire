(ns cheshire.parse
  (:import (com.fasterxml.jackson.core JsonParser JsonToken)))

(declare parse*)

(def ^:dynamic *chunk-size* 32)

(def ^{:doc "Flag to determine whether float values should be returned as
             BigDecimals to retain precision. Defaults to false."
       :dynamic true}
  *use-bigdecimals?* false)

(defmacro ^:private tag
  ([obj]
   `(vary-meta ~obj assoc :tag `JsonParser)))

(definline parse-kv-fn-map [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 key# (~key-fn key-str#)
                 value# (~value-fn key# (parse* ~jp ~key-fn ~value-fn ~bd? ~array-coerce-fn))
                 mmap# (assoc! mmap# key#
                               value#)]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-key-fn-map [^JsonParser jp key-fn bd? array-coerce-fn]
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
                               (parse* ~jp ~key-fn nil ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-basic-map [^JsonParser jp bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 mmap# (assoc! mmap# key-str#
                               (parse* ~jp nil nil ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-value-fn-map [^JsonParser jp value-fn bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 value# (~value-fn key-str# (parse* ~jp nil ~value-fn ~bd? ~array-coerce-fn))
                 mmap# (assoc! mmap# key-str#
                               value#)]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline parse-object
  [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  `(cond
     (and ~key-fn
          ~value-fn)
     (parse-kv-fn-map ~jp ~key-fn ~value-fn ~bd? ~array-coerce-fn)
     ~key-fn
     (parse-key-fn-map ~jp ~key-fn ~bd? ~array-coerce-fn)
     ~value-fn
     (parse-value-fn-map ~jp ~value-fn ~bd? ~array-coerce-fn)
     :else
     (parse-basic-map ~jp ~bd? ~array-coerce-fn)))

(definline parse-array-coerce-array [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient (~array-coerce-fn array-field-name#))]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                              (parse* ~jp ~key-fn ~value-fn ~bd? ~array-coerce-fn))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(definline parse-basic-array [^JsonParser jp key-fn value-fn bd?]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient [])]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                              (parse* ~jp ~key-fn ~value-fn ~bd? nil))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(definline parse-array [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  `(if (nil? ~array-coerce-fn)
     (parse-basic-array ~jp ~key-fn ~value-fn ~bd?)
     (parse-array-coerce-array ~jp ~key-fn ~value-fn ~bd? ~array-coerce-fn)))

(defn lazily-parse-array [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  (lazy-seq
   (loop [chunk-idx 0, buf (chunk-buffer *chunk-size*)]
     (if (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
       (chunk-cons (chunk buf) nil)
       (do
         (chunk-append buf (parse* jp key-fn value-fn bd? array-coerce-fn))
         (.nextToken jp)
         (let [chunk-idx* (unchecked-inc chunk-idx)]
           (if (< chunk-idx* *chunk-size*)
             (recur chunk-idx* buf)
             (chunk-cons
              (chunk buf)
              (lazily-parse-array jp key-fn value-fn bd? array-coerce-fn)))))))))

(defn parse* [^JsonParser jp key-fn value-fn bd? array-coerce-fn]
  (condp identical? (.getCurrentToken jp)
    JsonToken/START_OBJECT (parse-object jp key-fn value-fn bd? array-coerce-fn)
    JsonToken/START_ARRAY (parse-array jp key-fn value-fn bd? array-coerce-fn)
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

(defn parse-strict [^JsonParser jp key-fn value-fn eof array-coerce-fn]
  (let [key-fn (or (if (identical? key-fn true) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof
      (parse* jp key-fn value-fn *use-bigdecimals?* array-coerce-fn))))

(defn parse [^JsonParser jp key-fn value-fn eof array-coerce-fn]
  (.nextToken jp)
  (condp identical? (.getCurrentToken jp)
    nil
    eof

    JsonToken/START_ARRAY
    (do
      (.nextToken jp)
      (lazily-parse-array jp key-fn value-fn *use-bigdecimals?* array-coerce-fn))

    (parse* jp key-fn value-fn *use-bigdecimals?* array-coerce-fn)))