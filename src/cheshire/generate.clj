(ns cheshire.generate
  (:import (org.codehaus.jackson JsonGenerator)
           (java.util List Date SimpleTimeZone UUID)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (java.math BigInteger)
           (clojure.lang IPersistentCollection Keyword Ratio Symbol)))

(definline write-string [^JsonGenerator jg ^String str]
  `(.writeString ~jg ~str))

(definline fail [obj]
  `(throw (Exception. (str "Cannot JSON encode object of class: "
                           (class ~obj) ": " ~obj))))

(defmacro number-dispatch [^JsonGenerator jg obj]
  (if (< 2 (:minor *clojure-version*))
    (do
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
         (fail ~obj)))
    (do
      `(condp instance? ~obj
         Integer (.writeNumber ~jg (int ~obj))
         Long (.writeNumber ~jg (long ~obj))
         Double (.writeNumber ~jg (double ~obj))
         Float (.writeNumber ~jg (float ~obj))
         BigInteger (.writeNumber ~jg ^BigInteger ~obj)
         BigDecimal (.writeNumber ~jg ^BigDecimal ~obj)
         Ratio (.writeNumber ~jg (double ~obj))
         (fail ~obj)))))

(defn generate [^JsonGenerator jg obj ^String date-format]
  (let [generate-map (fn [^JsonGenerator jg m ^String date-format]
                       (do
                         (.writeStartObject jg)
                         (doseq [[k v] m
                                 :let [field-name (if (keyword? k)
                                                    (name k)
                                                    (str k))]]
                           (.writeFieldName jg field-name)
                           (generate jg v date-format))
                         (.writeEndObject jg)))
        generate-array (fn [^JsonGenerator jg a ^String date-format]
                         (do
                           (.writeStartArray jg)
                           (dotimes [i (count obj)]
                             (generate jg (.get ^List obj i) date-format))
                           (.writeEndArray jg)))]
    (condp instance? obj
      IPersistentCollection (condp instance? obj
                              clojure.lang.IPersistentMap
                              (generate-map jg obj date-format)
                              clojure.lang.IPersistentVector
                              (generate-array jg obj date-format)
                              clojure.lang.IPersistentSet
                              (generate jg (seq obj) date-format)
                              clojure.lang.ISeq
                              (do
                                (.writeStartArray jg)
                                (doseq [item obj]
                                  (generate jg item date-format))
                                (.writeEndArray jg)))
      java.util.Map (generate-map jg obj date-format)
      java.util.List (generate-array jg obj date-format)
      java.util.Set (generate jg (seq obj) date-format)
      Number (number-dispatch ^JsonGenerator jg obj)
      String (write-string ^JsonGenerator jg ^String obj)
      Keyword (write-string ^JsonGenerator jg
                            (if-let [ns (namespace obj)]
                              (str ns "/" (name obj))
                              (name obj)))
      UUID (write-string ^JsonGenerator jg (.toString ^UUID obj))
      Symbol (write-string ^JsonGenerator jg (if-let [sym (resolve obj)]
                                               (str (:ns (meta sym))
                                                    "/"
                                                    (:name (meta sym)))
                                               (.toString ^Symbol obj)))
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
          (catch Exception e (fail obj)))))))
