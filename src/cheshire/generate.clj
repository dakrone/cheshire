(ns cheshire.generate
  (:import (org.codehaus.jackson JsonGenerator JsonGenerationException)
           (java.util Date Map List Set SimpleTimeZone UUID)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (java.math BigInteger)
           (clojure.lang IPersistentCollection Keyword Ratio Symbol)))

(definline write-string [^JsonGenerator jg ^String str]
  `(.writeString ~jg ~str))

(definline fail [obj ^Exception e]
  `(throw (or ~e (JsonGenerationException.
                  (str "Cannot JSON encode object of class: "
                       (class ~obj) ": " ~obj)))))

(defmacro number-dispatch [^JsonGenerator jg obj ^Exception e]
  (if (< 2 (:minor *clojure-version*))
    `(condp instance? ~obj
       Integer (.writeNumber ~jg (int ~obj))
       Long (.writeNumber ~jg (long ~obj))
       Double (.writeNumber ~jg (double ~obj))
       Float (.writeNumber ~jg (double ~obj))
       BigInteger (.writeNumber ~jg ^BigInteger ~obj)
       BigDecimal (.writeNumber ~jg ^BigDecimal ~obj)
       Ratio (.writeNumber ~jg (double ~obj))
       clojure.lang.BigInt (.writeNumber ~jg ^clojure.lang.BigInt
                                         (.toBigInteger (bigint ~obj)))
       (fail ~obj ~e))
    `(condp instance? ~obj
       Integer (.writeNumber ~jg (int ~obj))
       Long (.writeNumber ~jg (long ~obj))
       Double (.writeNumber ~jg (double ~obj))
       Float (.writeNumber ~jg (float ~obj))
       BigInteger (.writeNumber ~jg ^BigInteger ~obj)
       BigDecimal (.writeNumber ~jg ^BigDecimal ~obj)
       Ratio (.writeNumber ~jg (double ~obj))
       (fail ~obj ~e))))

(declare generate)

(definline generate-map [^JsonGenerator jg obj ^String date-format ^Exception e]
  `(do
     (.writeStartObject ~jg)
     (doseq [[k# v#] ~obj]
       (.writeFieldName ~jg (if (keyword? k#)
                              (.substring (str k#) 1)
                              (str k#)))
       (generate ~jg v# ~date-format ~e))
     (.writeEndObject ~jg)))

(definline generate-array [^JsonGenerator jg obj ^String date-format
                           ^Exception e]
  `(do
     (.writeStartArray ~jg)
     (doseq [item# ~obj]
       (generate ~jg item# ~date-format ~e))
     (.writeEndArray ~jg)))

(defn generate [^JsonGenerator jg obj ^String date-format ^Exception ex]
  (condp instance? obj
    IPersistentCollection (condp instance? obj
                            clojure.lang.IPersistentMap
                            (generate-map jg obj date-format ex)
                            clojure.lang.IPersistentVector
                            (generate-array jg obj date-format ex)
                            clojure.lang.IPersistentSet
                            (generate jg (seq obj) date-format ex)
                            clojure.lang.IPersistentList
                            (generate-array jg obj date-format ex)
                            clojure.lang.ISeq
                            (generate-array jg obj date-format ex))
    Map (generate-map jg obj date-format ex)
    List (generate-array jg obj date-format ex)
    Set (generate jg (seq obj) date-format ex)
    Number (number-dispatch ^JsonGenerator jg obj ex)
    String (write-string ^JsonGenerator jg ^String obj )
    Keyword (write-string ^JsonGenerator jg
                          (if-let [ns (namespace obj)]
                            (str ns "/" (name obj))
                            (name obj)))
    UUID (write-string ^JsonGenerator jg (.toString ^UUID obj))
    Symbol (write-string ^JsonGenerator jg (.toString ^Symbol obj))
    Boolean (.writeBoolean ^JsonGenerator jg ^Boolean obj)
    Date (let [sdf (doto (SimpleDateFormat. date-format)
                     (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
           (write-string ^JsonGenerator jg (.format sdf obj)))
    Timestamp (let [date (Date. (.getTime ^Timestamp obj))
                    sdf (doto (SimpleDateFormat. date-format)
                          (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
                (write-string ^JsonGenerator jg (.format sdf obj)))
    (if (nil? obj)
      (.writeNull ^JsonGenerator jg)
      ;; it must be a primative then
      (try
        (.writeNumber ^JsonGenerator jg obj)
        (catch Exception e (fail obj ex))))))
