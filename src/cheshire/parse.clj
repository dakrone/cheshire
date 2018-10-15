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

(definline parse-object [^JsonParser jp key-fn bd? array-coerce-fn pred-xs detach-children?]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 key# (~key-fn key-str#)
                 pred# (or (first ~pred-xs) (constantly true))
                 match?# (pred# key#)
                 val# (when match?# (parse* ~jp ~key-fn ~bd? ~array-coerce-fn (next ~pred-xs) ~detach-children?))
                 mmap# (if match?#
                         (assoc! mmap# key# val#)
                         (do
                           (.skipChildren ~jp)
                           mmap#))]
             (.nextToken ~jp)
             (if (and ~detach-children? match?#)
               val#
               (recur mmap#)))
           (persistent! mmap#))))))

(definline parse-array [^JsonParser jp key-fn bd? array-coerce-fn pred-xs]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient (if ~array-coerce-fn
                                 (~array-coerce-fn array-field-name#)
                                 []))]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                              (parse* ~jp ~key-fn ~bd? ~array-coerce-fn ~pred-xs))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(defn lazily-parse-array
  ([^JsonParser jp key-fn bd? array-coerce-fn pred-xs]
   (let [pred-xs (or pred-xs (repeat (constantly true)))]
     (lazy-seq
      (loop [chunk-idx 0, buf (chunk-buffer 32)]
        (if (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
          (chunk-cons (chunk buf) nil)
          (do
            (chunk-append buf (parse* jp key-fn bd? array-coerce-fn pred-xs))
            (.nextToken jp)
            (let [chunk-idx* (unchecked-inc chunk-idx)]
              (if (< chunk-idx* 32)
                (recur chunk-idx* buf)
                (chunk-cons
                 (chunk buf)
                 (lazily-parse-array jp key-fn bd? array-coerce-fn pred-xs)))))))))))

(defn parse*
  ([jp key-fn bd? array-coerce-fn pred-xs] (parse* jp key-fn bd? array-coerce-fn pred-xs false))
  ([^JsonParser jp key-fn bd? array-coerce-fn pred-xs detach-children?]
   (condp identical? (.getCurrentToken jp)
     JsonToken/START_OBJECT (parse-object jp key-fn bd? array-coerce-fn pred-xs (if-not (first pred-xs)
                                                                                  false
                                                                                  detach-children?))
     JsonToken/START_ARRAY (if detach-children?
                             (lazily-parse-array jp key-fn bd? array-coerce-fn pred-xs)
                             (parse-array jp key-fn bd? array-coerce-fn pred-xs))
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
       (str "Cannot parse " (pr-str (.getCurrentToken jp))))))))

(defn parse-strict [^JsonParser jp key-fn eof array-coerce-fn pred-xs detach-children?]
  (let [key-fn (or (if (identical? key-fn true) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof
      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn pred-xs detach-children?))))

(defn parse [^JsonParser jp key-fn eof array-coerce-fn pred-xs detach-children?]
  (let [key-fn (or (if (and (instance? Boolean key-fn) key-fn) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof

      JsonToken/START_ARRAY
      (do
        (.nextToken jp)
        (lazily-parse-array jp key-fn *use-bigdecimals?* array-coerce-fn pred-xs))

      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn pred-xs detach-children?))))
