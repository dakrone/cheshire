(ns cheshire.generate
  (:use [cheshire.type-dispatch :only [deftypedispatched defdispatched]])
  (:import (org.codehaus.jackson JsonGenerator)
           (java.util List Date SimpleTimeZone UUID)
           (java.text SimpleDateFormat)
           (clojure.lang IPersistentCollection Keyword Symbol IPersistentMap
                         IPersistentVector IPersistentSet ISeq)))

(set! *warn-on-reflection* true)

(definline write-string [jg str]
  `(let [^JsonGenerator jg# ~jg
         ^String str# ~str]
     (.writeString jg# str#)))

(definline fail [obj & _]
  `(throw (Exception. (str "Cannot generate " ~obj))))

(declare generate)

(deftypedispatched generate*)

(defdispatched generate* :miss [obj ^JsonGenerator jg _]
  `(if (nil? ~obj)
     (.writeNull ~jg)
     (fail ~obj)))

(deftypedispatched generate-coll)

(defdispatched generate-coll IPersistentMap [obj ^JsonGenerator jg date-format]
  `(do
     (.writeStartObject ~jg)
     (doseq [[k# v#] ~obj
             :let [field-name# (if (keyword? k#)
                                 (name k#)
                                 (str k#))]]
       (.writeFieldName ~jg field-name#)
       (generate ~jg v# ~date-format))
     (.writeEndObject ~jg)))

(defdispatched generate-coll IPersistentVector [obj ^JsonGenerator jg
                                                date-format]
  `(do
     (.writeStartArray ~jg)
     (dotimes [i# (count ~obj)]
       (let [^List x# ~obj]
         (generate ~jg (.get x# i#) ~date-format)))
     (.writeEndArray ~jg)))

(defdispatched generate-coll IPersistentSet [obj ^JsonGenerator jg date-format]
  `(generate ~jg (seq ~obj) ~date-format))

(defdispatched generate-coll ISeq [obj ^JsonGenerator jg date-format]
  `(do
     (.writeStartArray ~jg)
     (doseq [item# ~obj]
       (generate ~jg item# ~date-format))
     (.writeEndArray ~jg)))

(defdispatched generate* IPersistentCollection [obj ^JsonGenerator jg
                                                date-format]
  `(generate-coll ~obj ~jg ~date-format))

(deftypedispatched generate-number)

(defdispatched generate-number :miss [obj jg]
  `(fail ~obj))

(defdispatched generate-number Integer [obj ^JsonGenerator jg]
  `(.writeNumber ~jg ~obj))

(defdispatched generate-number Long [obj ^JsonGenerator jg]
  `(.writeNumber ~jg ~obj))

(defdispatched generate-number Float [obj ^JsonGenerator jg]
  `(.writeNumber ~jg ~obj))

(defdispatched generate-number Double [obj ^JsonGenerator jg]
  `(.writeNumber ~jg ~obj))

(defdispatched generate-number BigInteger [obj ^JsonGenerator jg]
  `(.writeNumber ~jg ~obj))

(defdispatched generate* Number [obj ^JsonGenerator jg date-format]
  `(generate-number ~obj ~jg))

(defdispatched generate* String [obj jg date-format]
  `(write-string ~jg ~obj))

(defdispatched generate* Keyword [obj jg date-format]
  `(write-string ~jg (name ~obj)))

(defdispatched generate* UUID [obj jg date-format]
  `(write-string ~jg (.toString ~obj)))

(defdispatched generate* Symbol [obj jg date-format]
  `(write-string ~jg (.toString ~obj)))

(defdispatched generate* Boolean [obj jg date-format]
  `(let [^JsonGenerator jg# ~jg]
     (.writeBoolean jg# ~obj)))

(defdispatched generate* Date [obj jg date-format]
  `(let [sdf# (doto (SimpleDateFormat. ~date-format)
                (.setTimeZone (SimpleTimeZone. 0 "UTC")))]
     (write-string ~jg (.format sdf# ~obj))))

(defn generate [^JsonGenerator jg obj date-format]
  (generate* obj jg date-format))

;; the above all inlines into something like(infact even more stuff is
;; inlined into the below):

(comment
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
        (fail obj)))))
