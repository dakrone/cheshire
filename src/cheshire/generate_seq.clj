(ns cheshire.generate-seq
  "Namespace used to generate JSON from Clojure data structures in a
  sequential way."
  (:use [cheshire.generate :only [tag JSONable to-json i?
                                  number-dispatch write-string
                                  fail]])
  (:import (com.fasterxml.jackson.core JsonGenerator JsonGenerationException)
           (java.util Date Map List Set SimpleTimeZone UUID)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (java.math BigInteger)
           (clojure.lang IPersistentCollection Keyword Ratio Symbol)))

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
  [^JsonGenerator jg obj opts]
  (let [jg (tag jg)]
    `(let [wholeness# (:wholeness ~opts)
           inner-opts# (assoc ~opts :wholeness
                              (if (= wholeness# :start-inner)
                                :start
                                :all))]
       (write-start-object ~jg wholeness#)
       (doseq [m# ~obj]
         (let [k# (key m#)
               v# (val m#)]
           (.writeFieldName ~jg (if (keyword? k#)
                                  (.substring (str k#) 1)
                                  (str k#)))
           (generate ~jg v# inner-opts#)))
       (write-end-object ~jg wholeness#))))

(definline generate-key-fn-map
  [^JsonGenerator jg obj opts]
  (let [k (gensym 'k)
        name (gensym 'name)
        jg (tag jg)]
    `(let [key-fn# (:key-fn ~opts)
           wholeness# (:wholeness ~opts)
           inner-opts# (assoc ~opts :wholeness
                              (if (= wholeness# :start-inner)
                                :start
                                :all))]
       (write-start-object ~jg wholeness#)
       (doseq [m# ~obj]
         (let [~k (key m#)
               v# (val m#)
               ^String name# (if (keyword? ~k)
                               (key-fn# ~k)
                               (str ~k))]
           (.writeFieldName ~jg name#)
           (generate ~jg v# inner-opts#)))
       (write-end-object ~jg wholeness#))))

(definline generate-map
  [^JsonGenerator jg obj opts]
  `(if (nil? (:key-fn ~opts))
     (generate-basic-map ~jg ~obj ~opts)
     (generate-key-fn-map ~jg ~obj ~opts)))

(definline generate-array [^JsonGenerator jg obj opts]
  (let [jg (tag jg)]
    `(let [wholeness# (:wholeness ~opts)
           inner-opts# (assoc ~opts :wholeness
                              (if (= wholeness# :start-inner)
                                :start
                                :all))]
       (write-start-array ~jg wholeness#)
       (doseq [item# ~obj]
         (generate ~jg item# inner-opts#))
       (write-end-array ~jg wholeness#))))

(defn generate [^JsonGenerator jg obj opts]
  (let [opts (if (:wholeness opts)
               opts
               (assoc opts :wholeness :all))]
    (cond
      (nil? obj) (.writeNull ^JsonGenerator jg)
      (get (:impls JSONable) (class obj)) (#'to-json obj jg)

      (i? IPersistentCollection obj)
      (condp instance? obj
        clojure.lang.IPersistentMap
        (generate-map jg obj opts)
        clojure.lang.IPersistentVector
        (generate-array jg obj opts)
        clojure.lang.IPersistentSet
        (generate-array jg obj opts)
        clojure.lang.IPersistentList
        (generate-array jg obj opts)
        clojure.lang.ISeq
        (generate-array jg obj opts)
        clojure.lang.Associative
        (generate-map jg obj opts))

      (i? Number obj) (number-dispatch ^JsonGenerator jg obj (:ex opts))
      (i? Boolean obj) (.writeBoolean ^JsonGenerator jg ^Boolean obj)
      (i? String obj) (write-string ^JsonGenerator jg ^String obj)
      (i? Character obj) (write-string ^JsonGenerator jg ^String (str obj))
      (i? Keyword obj) (write-string ^JsonGenerator jg (.substring (str obj) 1))
      (i? Map obj) (generate-map jg obj opts)
      (i? List obj) (generate-array jg obj opts)
      (i? Set obj) (generate-array jg obj opts)
      (i? UUID obj) (write-string ^JsonGenerator jg (.toString ^UUID obj))
      (i? Symbol obj) (write-string ^JsonGenerator jg (.toString ^Symbol obj))
      (i? Date obj) (let [^String date-format (:date-format opts)
                          sdf (doto (SimpleDateFormat. date-format)
                                (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
                      (write-string ^JsonGenerator jg (.format sdf obj)))
      (i? Timestamp obj) (let [^String date-format (:date-format opts)
                               date (Date. (.getTime ^Timestamp obj))
                               sdf (doto (SimpleDateFormat. date-format)
                                     (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
                           (write-string ^JsonGenerator jg (.format sdf obj)))
      :else (fail obj jg (:ex opts)))))
