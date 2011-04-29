(ns cheshire.generate
  (:import (org.codehaus.jackson JsonGenerator)
           (java.util List Date SimpleTimeZone UUID)
           (java.text SimpleDateFormat)
           (clojure.lang IPersistentCollection Keyword Symbol)))

(definline write-string [^JsonGenerator jg ^String str]
  `(.writeString ~jg ~str))

(definline fail [obj]
  `(throw (Exception. (str "Cannot generate " (class ~obj) ": " ~obj))))

(defmacro number-dispatch [^JsonGenerator jg obj]
  (if (= 3 (:minor *clojure-version*))
    (do
      `(condp instance? ~obj
         Integer (.writeNumber ~jg (int ~obj))
         Long (.writeNumber ~jg (long ~obj))
         Double (.writeNumber ~jg (double ~obj))
         Float (.writeNumber ~jg (double ~obj))
         clojure.lang.BigInt (.writeNumber ~jg (.toBigInteger (bigint ~obj)))
         (fail ~obj)))
    (do
      `(condp instance? ~obj
         Integer (.writeNumber ~jg (int ~obj))
         Long (.writeNumber ~jg (long ~obj))
         Double (.writeNumber ~jg (double ~obj))
         Float (.writeNumber ~jg (float ~obj))
         BigInteger (.writeNumber ~jg ^BigInteger ~obj)
         (fail ~obj)))))

(defn generate [^JsonGenerator jg obj ^String date-format]
  (condp instance? obj
    IPersistentCollection (condp instance? obj
                            clojure.lang.IPersistentMap
                            (do
                              (.writeStartObject jg)
                              (doseq [[k v] obj
                                      :let [field-name (if (keyword? k)
                                                         (name k)
                                                         (str k))]]
                                (.writeFieldName jg field-name)
                                (generate jg v date-format))
                              (.writeEndObject jg))
                            clojure.lang.IPersistentVector
                            (do
                              (.writeStartArray jg)
                              (dotimes [i (count obj)]
                                (generate jg (.get ^List obj i) date-format))
                              (.writeEndArray jg))
                            clojure.lang.IPersistentSet
                            (generate jg (seq obj) date-format)
                            clojure.lang.ISeq
                            (do
                              (.writeStartArray jg)
                              (doseq [item obj]
                                (generate jg item date-format))
                              (.writeEndArray jg)))
    Number (number-dispatch ^JsonGenerator jg obj)
    String (write-string ^JsonGenerator jg ^String obj)
    Keyword (write-string ^JsonGenerator jg (name obj))
    UUID (write-string ^JsonGenerator jg (.toString obj))
    Symbol (write-string ^JsonGenerator jg (.toString obj))
    Boolean (.writeBoolean ^JsonGenerator jg ^Boolean obj)
    Date (let [sdf (doto (SimpleDateFormat. date-format)
                     (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
           (write-string ^JsonGenerator jg (.format sdf obj)))
    (if (nil? obj)
      (.writeNull ^JsonGenerator jg)
      ;; it must be a primative then
      (try
        (.writeNumber ^JsonGenerator jg obj)
        (catch Exception e (fail obj))))))
