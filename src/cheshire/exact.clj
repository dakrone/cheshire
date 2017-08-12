(ns cheshire.exact
  (:require [cheshire.factory :as factory]
            [cheshire.parse :as parse]
            [cheshire.core :as core])
  (:import (java.io StringReader Reader BufferedReader
                    Writer)
           (com.fasterxml.jackson.core JsonFactory)))

(defn- exact-parse [jp parsed]
  (let [valid-json? (try (nil? (.nextToken jp))
                         (catch Exception _ false))]
    (if valid-json?
      parsed
      (throw
       (IllegalArgumentException.
        "Invalid JSON, expected exactly one parseable object but multiple objects were found")))))

(defn parse-string
  "Like cheshire.core/parse-string
  but with only valid json string"
  ([string] (parse-string string nil nil))
  ([string key-fn] (parse-string string key-fn nil))
  ([^String string key-fn array-coerce-fn]
   (when string
     (let [jp (.createParser ^JsonFactory (or factory/*json-factory*
                                              factory/json-factory)
                             ^Reader (StringReader. string))]
       (exact-parse jp (parse/parse jp key-fn nil array-coerce-fn))))))

(defn parse-string-strict
  ([string] (parse-string-strict string nil nil))
  ([string key-fn] (parse-string-strict string key-fn nil))
  ([^String string key-fn array-coerce-fn]
   (when string
     (let [jp (.createParser ^JsonFactory (or factory/*json-factory*
                                              factory/json-factory)
                             ^Writer (StringReader. string))]
       (exact-parse jp (parse/parse-strict jp key-fn nil array-coerce-fn))))))

(def decode parse-string)
(core/copy-arglists decode parse-string)
(def decode-strict parse-string-strict)
(core/copy-arglists decode-strict parse-string-strict)
