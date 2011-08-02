(ns cheshire.factory
  "Factories used for JSON/SMILE generation, used by both the core and
 custom generators."
  (:import (org.codehaus.jackson.smile SmileFactory)
           (org.codehaus.jackson JsonFactory JsonParser$Feature)))

;; Factory objects that are needed to do the encoding and decoding
(def ^{:tag JsonFactory} factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

(def ^{:tag SmileFactory} smile-factory
  (SmileFactory.))

;; default date format used to JSON-encode Date objects
(def default-date-format "yyyy-MM-dd'T'HH:mm:ss'Z'")
