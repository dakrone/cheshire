(ns cheshire.generate-seq
  "Namespace used to generate JSON from Clojure data structures in a
  sequential way."
  (:require [cheshire.generate :as g])
  (:import (com.fasterxml.jackson.core JsonGenerator)
           (java.util Date Map List Set SimpleTimeZone UUID)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (clojure.lang IPersistentCollection Keyword Symbol)))

(definline write-start-object [^JsonGenerator jg wholeness]
  `(if (contains? #{:all :start :start-inner} ~wholeness)
     (.writeStartObject ~jg)))

(definline write-end-object [^JsonGenerator jg wholeness]
  `(if (contains? #{:all :end} ~wholeness)
     (.writeEndObject ~jg)))

(definline write-start-array [^JsonGenerator jg wholeness]
  `(if (contains? #{:all :start :start-inner} ~wholeness)
     (.writeStartArray ~jg)))

(definline write-end-array [^JsonGenerator jg wholeness]
  `(if (contains? #{:all :end} ~wholeness)
     (.writeEndArray ~jg)))

(declare generate)

(definline generate-basic-map
  [^JsonGenerator jg obj ^String date-format ^Exception e
   wholeness]
  (let [jg (g/tag jg)]
    `(do
       (write-start-object ~jg ~wholeness)
       (reduce (fn [^JsonGenerator jg# kv#]
                 (let [k# (key kv#)
                       v# (val kv#)]
                   (.writeFieldName jg# (if (keyword? k#)
                                          (.substring (str k#) 1)
                                          (str k#)))
                   (generate jg# v# ~date-format ~e nil
                             :wholeness (if (= ~wholeness :start-inner)
                                          :start
                                          :all))
                   jg#))
               ~jg ~obj)
       (write-end-object ~jg ~wholeness))))

(definline generate-key-fn-map
  [^JsonGenerator jg obj ^String date-format ^Exception e
   key-fn wholeness]
  (let [k (gensym 'k)
        name (gensym 'name)
        jg (g/tag jg)]
    `(do
       (write-start-object ~jg ~wholeness)
       (reduce (fn [^JsonGenerator jg# kv#]
                 (let [~k (key kv#)
                       v# (val kv#)
                       ^String name# (if (keyword? ~k)
                                       (~key-fn ~k)
                                       (str ~k))]
                   (.writeFieldName jg# name#)
                   (generate jg# v# ~date-format ~e ~key-fn
                             :wholeness (if (= ~wholeness :start-inner)
                                          :start
                                          :all))
                   jg#))
               ~jg ~obj)
       (write-end-object ~jg ~wholeness))))

(definline generate-map
  [^JsonGenerator jg obj ^String date-format ^Exception e
   key-fn wholeness]
  `(if (nil? ~key-fn)
     (generate-basic-map ~jg ~obj ~date-format ~e ~wholeness)
     (generate-key-fn-map ~jg ~obj ~date-format ~e ~key-fn ~wholeness)))

(definline generate-array [^JsonGenerator jg obj ^String date-format
                           ^Exception e key-fn wholeness]
  (let [jg (g/tag jg)]
    `(do
       (write-start-array ~jg ~wholeness)
       (reduce (fn [jg# item#]
                 (generate jg# item# ~date-format ~e ~key-fn
                           :wholeness (if (= ~wholeness :start-inner)
                                        :start
                                        :all))
                 jg#)
               ~jg ~obj)
       (write-end-array ~jg ~wholeness))))

(defn generate [^JsonGenerator jg obj ^String date-format
                ^Exception ex key-fn & {:keys [wholeness]}]
  (let [wholeness (or wholeness :all)]
    (cond
     (nil? obj) (.writeNull ^JsonGenerator jg)
     (get (:impls g/JSONable) (class obj)) (#'g/to-json obj jg)

     (g/i? IPersistentCollection obj)
     (condp instance? obj
       clojure.lang.IPersistentMap
       (generate-map jg obj date-format ex key-fn wholeness)
       clojure.lang.IPersistentVector
       (generate-array jg obj date-format ex key-fn wholeness)
       clojure.lang.IPersistentSet
       (generate-array jg obj date-format ex key-fn wholeness)
       clojure.lang.IPersistentList
       (generate-array jg obj date-format ex key-fn wholeness)
       clojure.lang.ISeq
       (generate-array jg obj date-format ex key-fn wholeness)
       clojure.lang.Associative
       (generate-map jg obj date-format ex key-fn wholeness))

     (g/i? Number obj) (g/number-dispatch ^JsonGenerator jg obj ex)
     (g/i? Boolean obj) (.writeBoolean ^JsonGenerator jg ^Boolean obj)
     (g/i? String obj) (g/write-string ^JsonGenerator jg ^String obj)
     (g/i? Character obj) (g/write-string ^JsonGenerator jg ^String (str obj))
     (g/i? Keyword obj) (g/write-string ^JsonGenerator jg (.substring (str obj) 1))
     (g/i? Map obj) (generate-map jg obj date-format ex key-fn wholeness)
     (g/i? List obj) (generate-array jg obj date-format ex key-fn wholeness)
     (g/i? Set obj) (generate-array jg obj date-format ex key-fn wholeness)
     (g/i? UUID obj) (g/write-string ^JsonGenerator jg (.toString ^UUID obj))
     (g/i? Symbol obj) (g/write-string ^JsonGenerator jg (.toString ^Symbol obj))
     (g/i? Date obj) (let [sdf (doto (SimpleDateFormat. date-format)
                               (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
                     (g/write-string ^JsonGenerator jg (.format sdf obj)))
     (g/i? Timestamp obj) (let [date (Date. (.getTime ^Timestamp obj))
                              sdf (doto (SimpleDateFormat. date-format)
                                    (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
                          (g/write-string ^JsonGenerator jg (.format sdf obj)))
     :else (g/fail obj jg ex))))
