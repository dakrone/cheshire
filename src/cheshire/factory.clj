(ns cheshire.factory
  "Factories used for JSON/SMILE generation, used by both the core and
 custom generators."
  (:import (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (com.fasterxml.jackson.dataformat.cbor CBORFactory)
           (com.fasterxml.jackson.core JsonFactory JsonFactory$Feature
                                       JsonParser$Feature)))

;; default date format used to JSON-encode Date objects
(def default-date-format "yyyy-MM-dd'T'HH:mm:ss'Z'")

(defonce default-factory-options
  {:auto-close-source false
   :allow-comments false
   :allow-unquoted-field-names false
   :allow-single-quotes false
   :allow-unquoted-control-chars true
   :allow-backslash-escaping false
   :allow-numeric-leading-zeros false
   :allow-non-numeric-numbers false
   :intern-field-names false
   :canonicalize-field-names false})

;; Factory objects that are needed to do the encoding and decoding
(defn make-json-factory
  ^JsonFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (doto (JsonFactory.)
      (.configure JsonParser$Feature/AUTO_CLOSE_SOURCE
                  (boolean (:auto-close-source opts)))
      (.configure JsonParser$Feature/ALLOW_COMMENTS
                  (boolean (:allow-comments opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_FIELD_NAMES
                  (boolean (:allow-unquoted-field-names opts)))
      (.configure JsonParser$Feature/ALLOW_SINGLE_QUOTES
                  (boolean (:allow-single-quotes opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS
                  (boolean (:allow-unquoted-control-chars opts)))
      (.configure JsonParser$Feature/ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
                  (boolean (:allow-backslash-escaping opts)))
      (.configure JsonParser$Feature/ALLOW_NUMERIC_LEADING_ZEROS
                  (boolean (:allow-numeric-leading-zeros opts)))
      (.configure JsonParser$Feature/ALLOW_NON_NUMERIC_NUMBERS
                  (boolean (:allow-non-numeric-numbers opts)))
      (.configure JsonFactory$Feature/INTERN_FIELD_NAMES
                  (boolean (:intern-field-names opts)))
      (.configure JsonFactory$Feature/CANONICALIZE_FIELD_NAMES
                  (boolean (:canonicalize-field-names opts))))))

(defn make-smile-factory
  ^SmileFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (doto (SmileFactory.)
      (.configure JsonParser$Feature/AUTO_CLOSE_SOURCE
                  (boolean (:auto-close-source opts)))
      (.configure JsonParser$Feature/ALLOW_COMMENTS
                  (boolean (:allow-comments opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_FIELD_NAMES
                  (boolean (:allow-unquoted-field-names opts)))
      (.configure JsonParser$Feature/ALLOW_SINGLE_QUOTES
                  (boolean (:allow-single-quotes opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS
                  (boolean (:allow-unquoted-control-chars opts)))
      (.configure JsonParser$Feature/ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
                  (boolean (:allow-backslash-escaping opts)))
      (.configure JsonParser$Feature/ALLOW_NUMERIC_LEADING_ZEROS
                  (boolean (:allow-numeric-leading-zeros opts)))
      (.configure JsonParser$Feature/ALLOW_NON_NUMERIC_NUMBERS
                  (boolean (:allow-non-numeric-numbers opts)))
      (.configure JsonFactory$Feature/INTERN_FIELD_NAMES
                  (boolean (:intern-field-names opts)))
      (.configure JsonFactory$Feature/CANONICALIZE_FIELD_NAMES
                  (boolean (:canonicalize-field-names opts))))))

(defn make-cbor-factory
  ^CBORFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (doto (CBORFactory.)
      (.configure JsonParser$Feature/AUTO_CLOSE_SOURCE
                  (boolean (:auto-close-source opts)))
      (.configure JsonParser$Feature/ALLOW_COMMENTS
                  (boolean (:allow-comments opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_FIELD_NAMES
                  (boolean (:allow-unquoted-field-names opts)))
      (.configure JsonParser$Feature/ALLOW_SINGLE_QUOTES
                  (boolean (:allow-single-quotes opts)))
      (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS
                  (boolean (:allow-unquoted-control-chars opts)))
      (.configure JsonParser$Feature/ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
                  (boolean (:allow-backslash-escaping opts)))
      (.configure JsonParser$Feature/ALLOW_NUMERIC_LEADING_ZEROS
                  (boolean (:allow-numeric-leading-zeros opts)))
      (.configure JsonParser$Feature/ALLOW_NON_NUMERIC_NUMBERS
                  (boolean (:allow-non-numeric-numbers opts)))
      (.configure JsonFactory$Feature/INTERN_FIELD_NAMES
                  (boolean (:intern-field-names opts)))
      (.configure JsonFactory$Feature/CANONICALIZE_FIELD_NAMES
                  (boolean (:canonicalize-field-names opts))))))

(defonce ^JsonFactory json-factory (make-json-factory default-factory-options))
(defonce ^SmileFactory smile-factory
  (make-smile-factory default-factory-options))
(defonce ^CBORFactory cbor-factory (make-cbor-factory default-factory-options))

;; dynamically rebindable json factory, if desired
(def ^{:dynamic true :tag JsonFactory} *json-factory* nil)
;; dynamically rebindable smile factory, if desired
(def ^{:dynamic true :tag SmileFactory} *smile-factory* nil)
;; dynamically rebindable cbor factory, if desired
(def ^{:dynamic true :tag CBORFactory} *cbor-factory* nil)
