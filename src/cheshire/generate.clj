(ns cheshire.generate
  "Namespace used to generate JSON from Clojure data structures."
  (:import (com.fasterxml.jackson.core JsonGenerator JsonGenerationException)
           (java.util Date Map List Set SimpleTimeZone UUID)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (java.math BigInteger)
           (clojure.lang IPersistentCollection Keyword Ratio Symbol)))

;; date format rebound for custom encoding
(def ^{:dynamic true :private true} *date-format*)

(defprotocol JSONable
  (to-json [t jg]))

(definline write-string [^JsonGenerator jg ^String str]
  `(.writeString ~jg ~str))

(defmacro fail [obj jg ^Exception e]
  `(try
     (to-json ~obj ~jg)
     (catch IllegalArgumentException _#
       (throw (or ~e (JsonGenerationException.
                      (str "Cannot JSON encode object of class: "
                           (class ~obj) ": " ~obj)))))))

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
       Short (.writeNumber ~jg (int ~obj))
       Byte (.writeNumber ~jg (int ~obj))
       clojure.lang.BigInt (.writeNumber ~jg ^clojure.lang.BigInt
                                         (.toBigInteger (bigint ~obj)))
       (fail ~obj ~jg ~e))
    `(condp instance? ~obj
       Integer (.writeNumber ~jg (int ~obj))
       Long (.writeNumber ~jg (long ~obj))
       Double (.writeNumber ~jg (double ~obj))
       Float (.writeNumber ~jg (float ~obj))
       BigInteger (.writeNumber ~jg ^BigInteger ~obj)
       BigDecimal (.writeNumber ~jg ^BigDecimal ~obj)
       Ratio (.writeNumber ~jg (double ~obj))
       Short (.writeNumber ~jg (int ~obj))
       Byte (.writeNumber ~jg (int ~obj))
       (fail ~obj ~jg ~e))))

(declare generate)

(definline generate-map [^JsonGenerator jg obj ^String date-format ^Exception e]
  `(do
     (.writeStartObject ~jg)
     (doseq [m# ~obj]
       (let [k# (key m#)
             v# (val m#)]
         (.writeFieldName ~jg (if (keyword? k#)
                                (.substring (str k#) 1)
                                (str k#)))
         (generate ~jg v# ~date-format ~e)))
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
                            (generate-array jg obj date-format ex)
                            clojure.lang.IPersistentList
                            (generate-array jg obj date-format ex)
                            clojure.lang.ISeq
                            (generate-array jg obj date-format ex)
                            clojure.lang.Associative
                            (generate-map jg obj date-format ex))
    Map (generate-map jg obj date-format ex)
    List (generate-array jg obj date-format ex)
    Set (generate-array jg obj date-format ex)
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
        (catch Exception e (fail obj jg ex))))))

;; Generic encoders, these can be used by someone writing a custom
;; encoder if so desired, after transforming an arbitrary data
;; structure into a clojure one, these can just be called.
(defn encode-nil
  "Encode null to the json generator."
  [_ ^JsonGenerator jg]
  (.writeNull jg))

(defn encode-str
  "Encode a string to the json generator."
  [^String s ^JsonGenerator jg]
  (.writeString jg (str s)))

(defn encode-number
  "Encode anything implementing java.lang.Number to the json generator."
  [^java.lang.Number n ^JsonGenerator jg]
  (.writeNumber jg n))

(defn encode-long
  "Encode anything implementing java.lang.Number to the json generator."
  [^Long n ^JsonGenerator jg]
  (.writeNumber jg (long n)))

(defn encode-int
  "Encode anything implementing java.lang.Number to the json generator."
  [n ^JsonGenerator jg]
  (.writeNumber jg (long n)))

(defn encode-ratio
  "Encode a clojure.lang.Ratio to the json generator."
  [^clojure.lang.Ratio n ^JsonGenerator jg]
  (.writeNumber jg (double n)))

(defn encode-seq
  "Encode a seq to the json generator."
  [s ^JsonGenerator jg]
  (.writeStartArray jg)
  (doseq [i s]
    (to-json i jg))
  (.writeEndArray jg))

(defn encode-date
  "Encode a date object to the json generator."
  [^Date d ^JsonGenerator jg]
  (let [sdf (SimpleDateFormat. *date-format*)]
    (.setTimeZone sdf (SimpleTimeZone. 0 "UTC"))
    (.writeString jg (.format sdf d))))

(defn encode-bool
  "Encode a Boolean object to the json generator."
  [^Boolean b ^JsonGenerator jg]
  (.writeBoolean jg b))

(defn encode-named
  "Encode a keyword to the json generator."
  [^clojure.lang.Keyword k ^JsonGenerator jg]
  (.writeString jg (if-let [ns (namespace k)]
                     (str ns "/" (name k))
                     (name k))))

(defn encode-map
  "Encode a clojure map to the json generator."
  [^clojure.lang.IPersistentMap m ^JsonGenerator jg]
  (.writeStartObject jg)
  (doseq [[k v] m]
    (.writeFieldName jg (if (instance? clojure.lang.Keyword k)
                          (if-let [ns (namespace k)]
                            (str ns "/" (name k))
                            (name k))
                          (str k)))
    (to-json v jg))
  (.writeEndObject jg))

(defn encode-symbol
  "Encode a clojure symbol to the json generator."
  [^clojure.lang.Symbol s ^JsonGenerator jg]
  (.writeString jg (str s)))

;; extended implementations for clojure datastructures
(extend nil
  JSONable
  {:to-json encode-nil})

(extend java.lang.String
  JSONable
  {:to-json encode-str})

;; This is lame, thanks for changing all the BigIntegers to BigInts
;; in 1.3 clojure/core :-/
(when (not= {:major 1 :minor 2} (select-keys *clojure-version* [:major :minor]))
  ;; Use Class/forName so it only resolves if it's running on clojure 1.3
  (extend (Class/forName "clojure.lang.BigInt")
    JSONable
    {:to-json (fn encode-bigint
                [^java.lang.Number n ^JsonGenerator jg]
                (.writeNumber jg ^java.math.BigInteger (.toBigInteger n)))}))

(extend clojure.lang.Ratio
  JSONable
  {:to-json encode-ratio})

(extend Long
  JSONable
  {:to-json encode-long})

(extend Short
  JSONable
  {:to-json encode-int})

(extend Byte
  JSONable
  {:to-json encode-int})

(extend java.lang.Number
  JSONable
  {:to-json encode-number})

(extend clojure.lang.ISeq
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentVector
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentSet
  JSONable
  {:to-json encode-seq})

(extend clojure.lang.IPersistentList
  JSONable
  {:to-json encode-seq})

(extend java.util.Date
  JSONable
  {:to-json encode-date})

(extend java.sql.Timestamp
  JSONable
  {:to-json #(encode-date (Date. (.getTime ^java.sql.Timestamp %1)) %2)})

(extend java.util.UUID
  JSONable
  {:to-json encode-str})

(extend java.lang.Boolean
  JSONable
  {:to-json encode-bool})

(extend clojure.lang.Keyword
  JSONable
  {:to-json encode-named})

(extend clojure.lang.IPersistentMap
  JSONable
  {:to-json encode-map})

(extend clojure.lang.Symbol
  JSONable
  {:to-json encode-symbol})

(extend clojure.lang.Associative
  JSONable
  {:to-json encode-map})

;; Utility methods to add and remove encoders
(defn add-encoder
  "Provide an encoder for a type not handled by Cheshire.

   ex. (add-encoder java.net.URL encode-string)

   See encode-str, encode-map, etc, in the cheshire.custom
   namespace for encoder examples."
  [cls encoder]
  (extend cls
    JSONable
    {:to-json encoder}))

(defn remove-encoder [cls]
  "Remove encoder for a given type.

   ex. (remove-encoder java.net.URL)"
  (alter-var-root #'JSONable #(assoc % :impls (dissoc (:impls %) cls)))
  (clojure.core/-reset-methods JSONable))
