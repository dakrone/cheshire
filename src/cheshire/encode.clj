(ns cheshire.encode
  (:require [cheshire.core :as core])
  (:import (java.io StringWriter)
           (java.util Date SimpleTimeZone)
           (java.text SimpleDateFormat)
           (org.codehaus.jackson JsonFactory JsonGenerator JsonParser
                                 JsonParser$Feature)))

(set! *warn-on-reflection* true)

(def ^{:private true :tag JsonFactory} factory
  (doto (JsonFactory.)
    (.configure JsonParser$Feature/ALLOW_UNQUOTED_CONTROL_CHARS true)))

(def ^{:dynamic true} *date-format* "yyyy-MM-dd'T'HH:mm:ss'Z'")

(defprotocol Jable
  (to-json [t jg]))

(defn encode [obj & _]
  (let [sw (StringWriter.)
        generator (.createJsonGenerator ^JsonFactory factory sw)]
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

(defn- encode-nil [_ ^JsonGenerator jg]
  (.writeNull jg))

(extend nil
  Jable
  {:to-json encode-nil})

(defn- encode-str [^String s ^JsonGenerator jg]
  (.writeString jg (str s)))

(extend java.lang.String
  Jable
  {:to-json encode-str})

(defn- encode-number [^java.lang.Number n ^JsonGenerator jg]
  (.writeNumber jg n))

(extend java.lang.Number
  Jable
  {:to-json encode-number})

(defn- encode-seq [s ^JsonGenerator jg]
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

(defn- encode-date [^Date d ^JsonGenerator jg]
  (let [sdf (SimpleDateFormat. *date-format*)]
    (.setTimeZone sdf (SimpleTimeZone. 0 "UTC"))
    (.writeString jg (.format sdf d))))

(extend java.util.Date
  Jable
  {:to-json encode-date})

(extend java.util.UUID
  Jable
  {:to-json encode-str})

(defn- encode-bool [^Boolean b ^JsonGenerator jg]
  (.writeBoolean jg b))

(extend java.lang.Boolean
  Jable
  {:to-json encode-bool})

(defn- encode-named [^clojure.lang.Keyword k ^JsonGenerator jg]
  (.writeString jg (name k)))

(extend clojure.lang.Keyword
  Jable
  {:to-json encode-named})

(defn- encode-map [^clojure.lang.IPersistentMap m ^JsonGenerator jg]
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

(defn- encode-symbol [^clojure.lang.Symbol s ^JsonGenerator jg]
  (.writeString jg (str (:ns (meta (resolve s)))
                        "/"
                        (:name (meta (resolve s))))))

(extend clojure.lang.Symbol
  Jable
  {:to-json encode-symbol})

