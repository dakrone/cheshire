(ns cheshire.experimental
  "Experimental JSON encoding/decoding tools."
  (:require [cheshire.core :refer :all]
            [clojure.java.io :refer :all]
            [tigris.core :refer [str-escaping-input-stream]])
  (:import (java.io ByteArrayInputStream SequenceInputStream)))

(defn encode-large-field-in-map
  "EXPERIMENTAL - SUBJECT TO CHANGE.

  Given a clojure object, a field name and a stream for a the string value of
  the field, return a stream that, when read, JSON encodes in a streamy way.

  An optional opt-map can be specified to pass enocding options to the map
  encoding, supports the same options as generate-string.

  Note that the input stream is not closed. You are responsible for closing it
  by calling .close() on the stream object returned from this method."
  ([obj field stream]
     (encode-large-field-in-map obj field stream nil))
  ([obj field stream & [opt-map]]
     (let [otherstr (encode (dissoc obj field) opt-map)
           truncstr (str (subs otherstr 0 (dec (count otherstr))))
           stream (str-escaping-input-stream stream)
           pre-stream (ByteArrayInputStream.
                       (.getBytes (str truncstr ",\"" (name field) "\":\"")))
           post-stream (ByteArrayInputStream.  (.getBytes "\"}"))]
       (reduce #(SequenceInputStream. %1 %2) [pre-stream stream post-stream]))))
