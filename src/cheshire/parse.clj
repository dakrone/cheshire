(ns cheshire.parse
  (:import (org.codehaus.jackson JsonParser JsonToken)))

(declare parse)

(definline parse-object [^JsonParser jp fst? keywords? eof array-coerce-fn]
  `(do
     (.nextToken ~jp)
     (loop [mmap# (transient {})]
       (if-not (= (.getCurrentToken ~jp)
                  JsonToken/END_OBJECT)
         (let [key-str# (.getText ~jp)
               _# (.nextToken ~jp)
               key# (if ~keywords?
                      (keyword key-str#)
                      key-str#)
               mmap# (assoc!
                      mmap#
                      key#
                      (parse
                       ~jp ~false ~keywords? ~eof ~array-coerce-fn))]
           (.nextToken ~jp)
           (recur mmap#))
         (persistent! mmap#)))))

(definline parse-array [^JsonParser jp fst? keywords? eof array-coerce-fn]
  `(let [array-field-name# (.getCurrentName ~jp)]
     (.nextToken ~jp)
     (loop [coll# (transient (if ~array-coerce-fn
                               (~array-coerce-fn array-field-name#)
                               []))]
       (if-not (= (.getCurrentToken ~jp)
                  JsonToken/END_ARRAY)
         (let [coll# (conj!
                      coll#
                      (parse
                       ~jp
                       false
                       ~keywords?
                       ~eof
                       ~array-coerce-fn))]
           (.nextToken ~jp)
           (recur coll#))
         (persistent! coll#)))))

(defn parse [^JsonParser jp fst? keywords? eof array-coerce-fn]
  (let [fst? (boolean fst?)
        keywords? (boolean keywords?)
        x (if fst?
            (do
              (.nextToken jp)
              (if (nil? (.getCurrentToken jp))
                eof
                (Object.)))
            (Object.))]
    (if (= x eof)
      eof
      (case (.toString (.getCurrentToken jp))
            "START_OBJECT" (parse-object jp fst? keywords? eof array-coerce-fn)
            "START_ARRAY" (parse-array jp fst? keywords? eof array-coerce-fn)
            "VALUE_STRING" (.getText jp)
            "VALUE_NUMBER_INT" (.getNumberValue jp)
            "VALUE_NUMBER_FLOAT" (.getDoubleValue jp)
            "VALUE_TRUE" true
            "VALUE_FALSE" false
            "VALUE_NULL" nil
            (throw
             (Exception.
              (str "Cannot parse " (pr-str (.getCurrentToken jp)))))))))
