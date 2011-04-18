(ns cheshire.parse
  (:import (org.codehaus.jackson JsonParser JsonToken)))

(declare parse)

(definline parse-object [^JsonParser jp fst? keywords? eof]
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
                       ~jp ~false ~keywords? ~eof))]
           (.nextToken ~jp)
           (recur mmap#))
         (persistent! mmap#)))))

(definline parse-array [^JsonParser jp fst? keywords? eof]
  `(do
     (.nextToken ~jp)
     (loop [vvec# (transient [])]
       (if-not (= (.getCurrentToken ~jp)
                  JsonToken/END_ARRAY)
         (let [vvec# (conj!
                     vvec#
                     (parse
                      ~jp
                      false
                      ~keywords?
                      ~eof))]
           (.nextToken ~jp)
           (recur vvec#))
         (persistent! vvec#)))))

(defn parse [^JsonParser jp fst? keywords? eof]
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
            "START_OBJECT" (parse-object jp fst? keywords? eof)
            "START_ARRAY" (parse-array jp fst? keywords? eof)
            "VALUE_STRING" (.getText jp)
            "VALUE_NUMBER_INT" (.getNumberValue jp)
            "VALUE_NUMBER_FLOAT" (.getDoubleValue jp)
            "VALUE_TRUE" true
            "VALUE_FALSE" false
            "VALUE_NULL" nil
            (throw
             (Exception.
              (str "Cannot parse " (pr-str (.getCurrentToken jp)))))))))
