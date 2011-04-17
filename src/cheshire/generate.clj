(ns cheshire.generate
  (:import (org.codehaus.jackson JsonGenerator)
           (java.util List Date SimpleTimeZone UUID)
           (java.text SimpleDateFormat)
           (clojure.lang IPersistentCollection Keyword Symbol)))

(definline write-string [^JsonGenerator jg ^String str]
  `(.writeString ~jg ~str))

(definline fail [obj]
  `(throw (Exception. (str "Cannot generate " ~obj))))

(defn generate [^JsonGenerator jg ^Object obj ^String date-format]
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
    Number (condp instance? obj
             Integer (.writeNumber jg ^Integer obj)
             Long (.writeNumber jg ^Long obj)
             Float (.writeNumber jg ^Float obj)
             Double (.writeNumber jg ^Double obj)
             BigInteger (.writeNumber jg ^BigInteger obj)
             (fail obj))
    String (write-string jg ^String obj)
    Keyword (write-string jg (name obj))
    UUID (write-string jg (.toString obj))
    Symbol (write-string jg (.toString obj))
    Boolean (.writeBoolean jg ^Boolean obj)
    Date (let [sdf (doto (SimpleDateFormat. date-format)
                     (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
           (write-string jg (.format sdf obj)))
    (if (nil? obj)
      (.writeNull jg)
      (fail obj))))
