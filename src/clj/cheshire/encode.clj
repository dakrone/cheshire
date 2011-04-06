(ns cheshire.encode
  (:require [cheshire.core :as core])
  (:import (java.io StringWriter)
           (java.util SimpleTimeZone)
           (java.text SimpleDateFormat)
           (org.codehaus.jackson JsonFactory JsonParser JsonParser$Feature)))

(def ^{:private true :type JsonFactory} factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

(def ^{:dynamic true} *date-format* "yyyy-MM-dd'T'HH:mm:ss'Z'")

(defprotocol Jable
  (to-json [t jg]))

(defn encode [obj & _]
  (let [sw (StringWriter.)
        generator (.createJsonGenerator factory sw)]
    (if obj
      (to-json obj generator)
      (.writeNull generator))
    (.flush generator)
    (.toString sw)))

(def parse-string core/parse-string)
(def parse-stream core/parse-stream)
(def parse-smile core/parse-smile)
(def parsed-seq core/parsed-seq)
(def decode core/decode)

(def generate-string encode)
(def generate-stream core/generate-stream)
(def generate-smile core/generate-smile)

(defn- encode-nil [_ jg]
  (.writeNull jg))

(extend nil
  Jable
  {:to-json encode-nil})

(defn- encode-str [s jg]
  (.writeString jg (str s)))

(extend java.lang.String
  Jable
  {:to-json encode-str})

(defn- encode-number [n jg]
  (.writeNumber jg n))

(extend java.lang.Integer
  Jable
  {:to-json encode-number})

(extend java.lang.Number
  Jable
  {:to-json encode-number})

(defn- encode-seq [s jg]
  (.writeStartArray jg)
  (doseq [i s]
    (to-json i jg))
  (.writeEndArray jg))

(extend clojure.lang.ISeq
  Jable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentVector
  Jable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentSet
  Jable
  {:to-json encode-seq})

(defn- encode-date [d jg]
  (let [sdf (SimpleDateFormat. *date-format*)]
    (.setTimeZone sdf (SimpleTimeZone. 0 "UTC"))
    (.writeString jg (.format sdf d))))

(extend java.util.Date
  Jable
  {:to-json encode-date})

(extend java.util.UUID
  Jable
  {:to-json encode-str})

(defn- encode-bool [b jg]
  (.writeBoolean jg b))

(extend java.lang.Boolean
  Jable
  {:to-json encode-bool})

(defn- encode-named [k jg]
  (.writeString jg (name k)))

(extend clojure.lang.Keyword
  Jable
  {:to-json encode-named})

(defn- encode-map [m jg]
  (.writeStartObject jg)
  (doseq [[k v] m]
    (.writeFieldName jg (if (instance? clojure.lang.Keyword k)
                          (name k)
                          (str k)))
    (to-json v jg))
  (.writeEndObject jg))

(extend clojure.lang.IPersistentMap
  Jable
  {:to-json encode-map})

(defn- encode-symbol [s jg]
  (.writeString jg (str (:ns (meta (resolve s)))
                        "/"
                        (:name (meta (resolve s))))))

(extend clojure.lang.Symbol
  Jable
  {:to-json encode-symbol})

