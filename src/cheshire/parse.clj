(ns cheshire.parse
  (:import (com.fasterxml.jackson.core JsonParser JsonToken)))

(defprotocol IParser
  :extend-via-metadata true
  (parse-object [this jp])
  (parse-object-key [this jp key-str])
  (parse-array [this jp])
  (parse-string [this jp])
  (parse-number-int [this jp])
  (parse-number-float [this jp])
  (parse-embedded-object [this jp]))

(declare parse*)

(def ^:dynamic *chunk-size* 32)

(def ^{:doc "Flag to determine whether float values should be returned as
             BigDecimals to retain precision. Defaults to false."
       :dynamic true
       :deprecated true}
  *use-bigdecimals?* false)

(defmacro ^:private tag
  ([obj]
     `(vary-meta ~obj assoc :tag `JsonParser)))

(definline default-parse-object [p ^JsonParser jp]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [mmap# (transient {})]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_OBJECT)
           (let [key-str# (.getText ~jp)
                 _# (.nextToken ~jp)
                 key# (parse-object-key ~p ~jp key-str#)
                 mmap# (assoc! mmap# key#
                               (parse* ~p ~jp))]
             (.nextToken ~jp)
             (recur mmap#))
           (persistent! mmap#))))))

(definline default-parse-array [p ^JsonParser jp seed]
  (let [jp (tag jp)]
    `(do
       (.nextToken ~jp)
       (loop [coll# (transient ~seed)]
         (if-not (identical? (.getCurrentToken ~jp)
                             JsonToken/END_ARRAY)
           (let [coll# (conj! coll#
                              (parse* ~p ~jp))]
             (.nextToken ~jp)
             (recur coll#))
           (persistent! coll#))))))

(defn lazily-parse-array [p ^JsonParser jp]
  (lazy-seq
   (loop [chunk-idx 0, buf (chunk-buffer *chunk-size*)]
     (if (identical? (.getCurrentToken jp) JsonToken/END_ARRAY)
       (chunk-cons (chunk buf) nil)
       (do
         (chunk-append buf (parse* p jp))
         (.nextToken jp)
         (let [chunk-idx* (unchecked-inc chunk-idx)]
           (if (< chunk-idx* *chunk-size*)
             (recur chunk-idx* buf)
             (chunk-cons
              (chunk buf)
              (lazily-parse-array p jp)))))))))

(defn default-parse-string [p ^JsonParser jp]
  (.getText jp))

(defn default-parse-number-int [p ^JsonParser jp]
  (.getNumberValue jp))

(defn default-parse-number-float [p ^JsonParser jp]
  (.getNumberValue jp))

(defn default-parse-embedded-object [p ^JsonParser jp]
  (.getBinaryValue jp))

(defn default-parse-object-key [p jp key-str]
  key-str)

(defn make-parser [{::syms [parse-object
                            parse-object-key
                            parse-array
                            parse-string
                            parse-number-int
                            parse-number-float
                            parse-embedded-object]
                    :or    {parse-object          default-parse-object
                            parse-object-key      default-parse-object-key
                            parse-array           default-parse-array
                            parse-string          default-parse-string
                            parse-number-int      default-parse-number-int
                            parse-number-float    default-parse-number-float
                            parse-embedded-object default-parse-embedded-object}}]
  (cond (and (identical? parse-object default-parse-object)
             (identical? parse-array default-parse-array))
        (reify IParser
          (parse-object [this jp]
            (default-parse-object this jp))
          (parse-object-key [this jp key-str]
            (parse-object-key this jp key-str))
          (parse-array [this jp]
            (default-parse-array this jp []))
          (parse-string [this jp]
            (parse-string this jp))
          (parse-number-int [this jp]
            (parse-number-int this jp))
          (parse-number-float [this jp]
            (parse-number-float this jp))
          (parse-embedded-object [this jp]
            (parse-embedded-object this jp)))

        (identical? parse-object default-parse-object)
        (reify IParser
          (parse-object [this jp]
            (default-parse-object this jp))
          (parse-object-key [this jp key-str]
            (parse-object-key this jp key-str))
          (parse-array [this jp]
            (parse-array this jp))
          (parse-string [this jp]
            (parse-string this jp))
          (parse-number-int [this jp]
            (parse-number-int this jp))
          (parse-number-float [this jp]
            (parse-number-float this jp))
          (parse-embedded-object [this jp]
            (parse-embedded-object this jp)))

        :else
        (reify IParser
          (parse-object [this jp]
            (parse-object this jp))
          (parse-object-key [this jp key-str]
            (parse-object-key this jp key-str))
          (parse-array [this jp]
            (parse-array this jp))
          (parse-string [this jp]
            (parse-string this jp))
          (parse-number-int [this jp]
            (parse-number-int this jp))
          (parse-number-float [this jp]
            (parse-number-float this jp))
          (parse-embedded-object [this jp]
            (parse-embedded-object this jp)))))

(def default-parser
  (make-parser {}))

(defn parse-number-decimal [p ^JsonParser jp]
  (.getDecimalValue ^JsonParser jp))

(defn parse-object-key-with-key-fn [key-fn]
  (let [key-fn (if (boolean? key-fn)
                 keyword
                 key-fn)]
    (fn [_ _ key-str]
      (key-fn key-str))))

(defn parse-array-with-coerce-fn [array-coerce-fn]
  (fn [p ^JsonParser jp]
    (let [array-field-name (.getCurrentName jp)]
      (default-parse-array p jp (array-coerce-fn array-field-name)))))

(defn backwards-compatible-parser
  ([key-fn]
   (backwards-compatible-parser key-fn nil))
  ([key-fn array-coerce-fn]
   (backwards-compatible-parser key-fn array-coerce-fn *use-bigdecimals?*))
  ([key-fn array-coerce-fn use-big-decimals?]
   (if (or key-fn array-coerce-fn use-big-decimals?)
     (cond-> {}
       key-fn
       (assoc `parse-object-key (parse-object-key-with-key-fn key-fn))

       (some? array-coerce-fn)
       (assoc `parse-array (parse-array-with-coerce-fn array-coerce-fn))

       use-big-decimals?
       (assoc `parse-number-float parse-number-decimal)

       :always
       (make-parser))
     default-parser)))

(defn parse* [p ^JsonParser jp]
  (condp identical? (.getCurrentToken jp)
    JsonToken/START_OBJECT          (parse-object p jp)
    JsonToken/START_ARRAY           (parse-array p jp)
    JsonToken/VALUE_STRING          (parse-string p jp)
    JsonToken/VALUE_NUMBER_INT      (parse-number-int p jp)
    JsonToken/VALUE_NUMBER_FLOAT    (parse-number-float p jp)
    JsonToken/VALUE_EMBEDDED_OBJECT (parse-embedded-object p jp)
    JsonToken/VALUE_TRUE            true
    JsonToken/VALUE_FALSE           false
    JsonToken/VALUE_NULL            nil
    (throw
     (Exception.
      (str "Cannot parse " (pr-str (.getCurrentToken jp)))))))

(defn parse-strict [p ^JsonParser jp eof]
  (.nextToken jp)
  (condp identical? (.getCurrentToken jp)
    nil
    eof
    (parse* p jp)))

(defn parse [p ^JsonParser jp eof]
  (.nextToken jp)
  (condp identical? (.getCurrentToken jp)
    nil
    eof

    JsonToken/START_ARRAY
    (do
      (.nextToken jp)
      (lazily-parse-array p jp))

    (parse* p jp)))
