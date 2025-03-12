(ns cheshire.factory
  "Factories used for JSON/SMILE generation, used by both the core and
 custom generators."
  (:import (com.fasterxml.jackson.dataformat.smile SmileFactory)
           (com.fasterxml.jackson.dataformat.cbor CBORFactory)
           (com.fasterxml.jackson.core TSFBuilder JsonFactory JsonFactory$Feature
                                       StreamReadFeature
                                       StreamReadConstraints StreamWriteConstraints)
           (com.fasterxml.jackson.core.json JsonReadFeature
                                            JsonWriteFeature)))

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
   :canonicalize-field-names false
   :quote-field-names true
   :strict-duplicate-detection false
   ;; default values from Jackson 2.18.3
   ;; as of this version seem to be enforced for json only and not cbor, smile
   :max-input-document-length nil ;; no limit by default
   :max-input-token-count nil     ;; no limit by default
   :max-input-name-length 50000
   :max-input-nesting-depth 1000
   :max-input-number-length 1000
   :max-input-string-length 20000000
   :max-output-nesting-depth 1000})

(defn- apply-base-opts ^TSFBuilder [^TSFBuilder builder opts]
  (-> builder
      (.configure StreamReadFeature/AUTO_CLOSE_SOURCE
                  (boolean (:auto-close-source opts)))
      (.configure StreamReadFeature/STRICT_DUPLICATE_DETECTION
                  (boolean (:strict-duplicate-detection opts)))
      (.configure JsonFactory$Feature/INTERN_FIELD_NAMES
                  (boolean (:intern-field-names opts)))
      (.configure JsonFactory$Feature/CANONICALIZE_FIELD_NAMES
                  (boolean (:canonicalize-field-names opts)))
      (.streamReadConstraints (-> (StreamReadConstraints/builder)
                                  (.maxDocumentLength (or (:max-input-document-length opts) -1))
                                  (.maxTokenCount (or (:max-input-token-count opts) -1))
                                  (.maxNameLength (:max-input-name-length opts))
                                  (.maxNestingDepth (:max-input-nesting-depth opts))
                                  (.maxNumberLength (:max-input-number-length opts))
                                  (.maxStringLength (:max-input-string-length opts))
                                  (.build)))
      (.streamWriteConstraints (-> (StreamWriteConstraints/builder)
                                   (.maxNestingDepth (:max-output-nesting-depth opts))
                                   (.build)))))

(defn- apply-json-opts ^TSFBuilder [^TSFBuilder builder opts]
  (-> builder
      (.configure JsonReadFeature/ALLOW_JAVA_COMMENTS
                  (boolean (:allow-comments opts)))
      (.configure JsonReadFeature/ALLOW_UNQUOTED_FIELD_NAMES
                  (boolean (:allow-unquoted-field-names opts)))
      (.configure JsonReadFeature/ALLOW_SINGLE_QUOTES
                  (boolean (:allow-single-quotes opts)))
      (.configure JsonReadFeature/ALLOW_UNESCAPED_CONTROL_CHARS
                  (boolean (:allow-unquoted-control-chars opts)))
      (.configure JsonReadFeature/ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
                  (boolean (:allow-backslash-escaping opts)))
      (.configure JsonReadFeature/ALLOW_LEADING_ZEROS_FOR_NUMBERS
                  (boolean (:allow-numeric-leading-zeros opts)))
      (.configure JsonReadFeature/ALLOW_NON_NUMERIC_NUMBERS
                  (boolean (:allow-non-numeric-numbers opts)))
      (.configure JsonWriteFeature/QUOTE_FIELD_NAMES
                  (boolean (:quote-field-names opts)))))

;; Factory objects that are needed to do the encoding and decoding
(defn make-json-factory
  ^JsonFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (-> (JsonFactory/builder)
        (apply-base-opts opts)
        (apply-json-opts opts)
        (.build))))

(defn make-smile-factory
  ^SmileFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (-> (SmileFactory/builder)
        (apply-base-opts opts)
        (.build))))

(defn make-cbor-factory
  ^CBORFactory [opts]
  (let [opts (merge default-factory-options opts)]
    (-> (CBORFactory/builder)
        (apply-base-opts opts)
        (.build))))

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
