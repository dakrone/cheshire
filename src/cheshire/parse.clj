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

(definline parse-object [^JsonParser jp key-fn bd? array-coerce-fn parent-path path-predicate lazy? leaves-only?]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (or (and ~lazy? (= 1 (count mmap#)))
                     (identical? (.getCurrentToken ~jp)
                                 JsonToken/END_OBJECT))
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 parent-path# (conj ~parent-path key-str#)
                 mmap# (if (or (nil? ~path-predicate)
                               (~path-predicate parent-path#))
                         (assoc! mmap# (~key-fn key-str#)
                                 (parse* ~jp ~key-fn ~bd? ~array-coerce-fn parent-path# ~path-predicate ~lazy? ~leaves-only?))
                         (do
                           (.skipChildren ~jp)
                           mmap#))]
             (.nextToken ~jp)
             (recur mmap#))
           ((if (or ~lazy? ~leaves-only?) #(-> % vals first) identity)
            (persistent! mmap#)))))))

(definline parse-array [^JsonParser jp key-fn bd? array-coerce-fn parent-path path-predicate leaves-only?]
  (let [jp (tag jp)]
    `(let [array-field-name# (.getCurrentName ~jp)]
       (.nextToken ~jp)
       (loop [coll# (transient (if ~array-coerce-fn
                                 (~array-coerce-fn array-field-name#)
                                 []))
              counter# 0]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [parent-path# (conj ~parent-path counter#)
                 coll# (if (or (nil? ~path-predicate)
                               (~path-predicate parent-path#))
                         (conj! coll#
                                (parse* ~jp ~key-fn ~bd? ~array-coerce-fn parent-path# ~path-predicate false ~leaves-only?))
                         (do
                           (.skipChildren ~jp)
                           coll#))]
             (.nextToken ~jp)
             (recur coll# (inc counter#)))
           (persistent! coll#))))))

(defn lazily-parse-array [^JsonParser jp key-fn bd? array-coerce-fn parent-path path-predicate position leaves-only?]
  (lazy-seq
   (when-not (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
     (loop [position position]
       (let [parent-path* (conj parent-path position)]
         (if (or (nil? path-predicate)
                 (path-predicate parent-path*))
           (cons (let [val (parse* jp key-fn bd? array-coerce-fn parent-path* path-predicate false leaves-only?)]
                   (.nextToken jp)
                   val)
                 (lazily-parse-array jp key-fn bd? array-coerce-fn parent-path path-predicate (inc position) leaves-only?))
           (do
             (.skipChildren jp)
             (.nextToken jp)
             (recur (inc position)))))))))

(defn parse* [^JsonParser jp key-fn bd? array-coerce-fn parent-path path-predicate lazy? leaves-only?]
  (condp identical? (.getCurrentToken jp)
    JsonToken/START_OBJECT (parse-object jp key-fn bd? array-coerce-fn parent-path path-predicate lazy? leaves-only?)
    JsonToken/START_ARRAY (if lazy?
                            (lazily-parse-array jp key-fn bd? array-coerce-fn parent-path path-predicate 0 leaves-only?)
                            (parse-array jp key-fn bd? array-coerce-fn parent-path path-predicate leaves-only?))
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

(defn parse-strict [^JsonParser jp key-fn eof array-coerce-fn path-predicate lazy? leaves-only?]
  (let [key-fn (or (if (and (instance? Boolean key-fn) key-fn) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof
      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn [] path-predicate lazy? leaves-only?))))

(defn parse [^JsonParser jp key-fn eof array-coerce-fn path-predicate leaves-only?]
  (let [key-fn (or (if (and (instance? Boolean key-fn) key-fn) keyword key-fn) identity)]
    (.nextToken jp)
    (condp identical? (.getCurrentToken jp)
      nil
      eof

      JsonToken/START_ARRAY
      (do
        (.nextToken jp)
        (lazily-parse-array jp key-fn *use-bigdecimals?* array-coerce-fn [] path-predicate 0 leaves-only?))

      (parse* jp key-fn *use-bigdecimals?* array-coerce-fn [] path-predicate false leaves-only?))))
