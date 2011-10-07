# Cheshire

<img src="http://dakrone.github.com/cheshire/cheshire_small.jpg"
title=":)" align="left" padding="5px" />
<small>
'Cheshire Puss,' she began, rather timidly, as she did not at all know
whether it would like the name: however, it only grinned a little
wider.  'Come, it's pleased so far,' thought Alice, and she went
on. 'Would you tell me, please, which way I ought to go from here?'

'That depends a good deal on where you want to get to,' said the Cat.

'I don't much care where--' said Alice.

'Then it doesn't matter which way you go,' said the Cat.

'--so long as I get SOMEWHERE,' Alice added as an explanation.

'Oh, you're sure to do that,' said the Cat, 'if you only walk long
enough.'
</small>
<br clear=all /><br />
Cheshire is fast JSON encoding, based off of clj-json and
clojure-json, with additional features like Date/UUID/Set/Symbol
encoding and SMILE support.

[Clojure code with docs](http://dakrone.github.com/cheshire/)

[![Continuous Integration status](https://secure.travis-ci.org/dakrone/cheshire.png)](http://travis-ci.org/dakrone/cheshire)

## Why?

clojure-json had really nice features (custom encoders), but was slow;
clj-json had no features, but was fast. Cheshire encodes JSON fast,
with the ability to use custom encoders.

## Usage

    [cheshire "2.0.2"]
    
    Cheshire v2.0.2 uses Jackson 1.8.5

    ;; In your ns statement:
    (ns myns
      (:use [cheshire.core]))

### Encoding

```clojure
;; generate some json
(generate-string {:foo "bar" :baz 5})

;; write some json to a stream
(generate-stream {:foo "bar" :baz 5} (clojure.java.io/writer "/tmp/foo"))

;; generate some SMILE
(generate-smile {:foo "bar" :baz 5})

;; generate some JSON with Dates
;; the Date will be encoded as a string using
;; the default date format: yyyy-MM-dd'T'HH:mm:ss'Z'
(generate-string {:foo "bar" :baz (Date. 0)})

;; generate some JSON with Dates with custom Date encoding
(generate-string {:baz (Date. 0)} "yyyy-MM-dd")
```

### Decoding

```clojure
;; parse some json
(parse-string "{\"foo\":\"bar\"}")
;; => {"foo" "bar"}

;; parse some json and get keywords back
(parse-string "{\"foo\":\"bar\"}" true)
;; => {:foo "bar"}

;; parse some SMILE (keywords option also supported)
(parse-smile <your-byte-array>)

;; parse a stream (keywords option also supported)
(parse-stream (clojure.java.io/reader "/tmp/foo"))

;; parse a stream lazily (keywords option also supported)
(parsed-seq (clojure.java.io/reader "/tmp/foo"))

;; parse a SMILE stream lazily (keywords option also supported)
(parsed-smile-seq (clojure.java.io/reader "/tmp/foo"))
```

### Custom Encoders

Custom encoding is supported from 2.0.0 and up, however there still
may be bugs, if you encounter a bug, please open a github issue.

```clojure
;; Custom encoders allow you to swap out the api for the fast
;; encoder with one that is slightly slower, but allows custom
;; things to be encoded:
(ns myns
  (:use [cheshire.custom]))

;; First, add a custom encoder for a class:
(add-encoder java.awt.Color
             (fn [c jsonGenerator]
               (.writeString jsonGenerator (str c))))

;; There are also helpers for common encoding actions:
(add-encoder java.net.URL encode-str)

;; List of common encoders that can be used: (see custom.clj)
;; encode-nil
;; encode-number
;; encode-seq
;; encode-date
;; encode-bool
;; encode-named
;; encade-map
;; encade-symbol

;; Then you can use encode from the custom namespace as normal
(encode (java.awt.Color. 1 2 3))
;; => "java.awt.Color[r=1,g=2,b=3]"

;; Custom encoders can also be removed:
(remove-encoder java.awt.Color)

;; Decoding remains the same, you are responsible for doing custom decoding.
```

Custom (slower) and Core (faster) encoding can be mixed and matched by
requiring both namespaces and using the custom one only when you need
to encode custom classes. The API methods for cheshire.core and
cheshire.custom are exactly the same (except for add-encoder and
remove-encoder in the custom namespace).

There are also a few aliases for commonly used functions:

    encode -> generate-string
    encode-stream -> generate-stream
    encode-smile -> generate-smile
    decode -> parse-string
    decode-stream -> parse-stream
    decode-smile -> parse-smile

## Features
Cheshire supports encoding standard clojure datastructures, but with a
few addons. Right now it does not support custom encoders, but a few
helpers were added to support commonly encoded classes.

Cheshire encoding supports:

### Clojure data structures
- strings
- lists
- vectors
- sets
- maps
- symbols
- booleans
- keywords (qualified and unqualified)
- numbers (Integer, Long, BigInteger, BigInt, Double, Float, primatives)

### Java classes
- Date
- UUID
- java.sql.Timestamp

### Custom class encoding while still being fast

### Also supports
- Stream encoding/decoding
- Lazy decoding
- Replacing default encoders for builtin types
- [SMILE encoding/decoding](http://wiki.fasterxml.com/SmileFormatSpec)

## Speed

    Clojure version:  1.2.1
    Num roundtrips:   100000

    Trial:  1
    clj-json                               2.16
    clj-json w/ keywords                   2.43
    clj-serializer                         2.13
    cheshire                               2.19
    cheshire-smile                         2.20
    cheshire w/ keywords                   2.12
    clojure printer/reader                 7.16
    clojure printer/reader w/ print-dup    12.29
    clojure-json                           20.55
    clojure.data.json                      11.28
    
    Trial:  2
    clj-json                               1.23
    clj-json w/ keywords                   2.17
    clj-serializer                         1.58
    cheshire                               1.49
    cheshire-smile                         1.49
    cheshire w/ keywords                   1.99
    clojure printer/reader                 5.97
    clojure printer/reader w/ print-dup    11.17
    clojure-json                           20.42
    clojure.data.json                      11.25


Benchmarks for custom encoding coming soon.

## Future Ideas/TODOs
- <del>move away from using Java entirely, use Protocols for the
  custom encoder</del> (see custom.clj)
- <del>allow custom encoders</del> (see custom.clj)
- <del>figure out a way to encode namespace-qualified keywords</del>
- look into overriding the default encoding handlers with custom handlers
- better handling when java numbers overflow ECMAScript's numbers
  (-2^31 to (2^31 - 1))
- <del>handle encoding java.sql.Timestamp the same as
  java.util.Date</del>
- make it as fast as possible

## License
Release under the MIT license. See LICENSE for the full license.

## Thanks
Thanks go to Mark McGranaghan for allowing me to look at the clj-json
code to get started on this and Jim Duey for the name. :)
