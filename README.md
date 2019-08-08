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
with added support for more types and the ability to use custom
encoders.

## Usage

```clojure
[cheshire "5.9.0"]

;; Cheshire v5.9.0 uses Jackson 2.9.9

;; In your ns statement:
(ns my.ns
  (:require [cheshire.core :refer :all]))
```

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
(generate-string {:foo "bar" :baz (java.util.Date. 0)})

;; generate some JSON with Dates with custom Date encoding
(generate-string {:baz (java.util.Date. 0)} {:date-format "yyyy-MM-dd"})

;; generate some JSON with pretty formatting
(generate-string {:foo "bar" :baz {:eggplant [1 2 3]}} {:pretty true})
;; {
;;   "foo" : "bar",
;;   "baz" : {
;;     "eggplant" : [ 1, 2, 3 ]
;;   }
;; }

;; generate JSON escaping UTF-8
(generate-string {:foo "It costs £100"} {:escape-non-ascii true})
;; => "{\"foo\":\"It costs \\u00A3100\"}"

;; generate JSON and munge keys with a custom function
(generate-string {:foo "bar"} {:key-fn (fn [k] (.toUpperCase (name k)))})
;; => "{\"FOO\":\"bar\"}"
```

In the event encoding fails, Cheshire will throw a JsonGenerationException.

#### Custom Pretty Printing Options

If Jackson's default pretty printing library is not what you desire, you can
manually create your own pretty printing class and pass to the `generate-string`
or `encode` methods:

```clojure
(let [my-pretty-printer (create-pretty-printer
                          (assoc default-pretty-print-options
                                 :indent-arrays? true))]
  (generate-string {:foo [1 2 3]} {:pretty my-pretty-printer}))
```

See the `default-pretty-print-options` for a list of options that can be
changed.

### Decoding

```clojure
;; parse some json
(parse-string "{\"foo\":\"bar\"}")
;; => {"foo" "bar"}

;; parse some json and get keywords back
(parse-string "{\"foo\":\"bar\"}" true)
;; => {:foo "bar"}

;; parse some json and munge keywords with a custom function
(parse-string "{\"foo\":\"bar\"}" (fn [k] (keyword (.toUpperCase k))))
;; => {:FOO "bar"}

;; top-level strings are valid JSON too
(parse-string "\"foo\"")
;; => "foo"

;; parse some SMILE (keywords option also supported)
(parse-smile <your-byte-array>)

;; parse a stream (keywords option also supported)
(parse-stream (clojure.java.io/reader "/tmp/foo"))

;; parse a stream lazily (keywords option also supported)
(parsed-seq (clojure.java.io/reader "/tmp/foo"))

;; parse a SMILE stream lazily (keywords option also supported)
(parsed-smile-seq (clojure.java.io/reader "/tmp/foo"))
```

In 2.0.4 and up, Cheshire allows passing in a
function to specify what kind of types to return, like so:

```clojure
;; In this example a function that checks for a certain key
(decode "{\"myarray\":[2,3,3,2],\"myset\":[1,2,2,1]}" true
        (fn [field-name]
          (if (= field-name "myset")
            #{}
            [])))
;; => {:myarray [2 3 3 2], :myset #{1 2}}
```
The type must be "transient-able", so use either #{} or []


### Custom Encoders

Custom encoding is supported from 2.0.0 and up, if you encounter a
bug, please open a github issue. From 5.0.0 onwards, custom encoding
has been moved to be part of the core namespace (not requiring a
namespace change)

```clojure
;; Custom encoders allow you to swap out the api for the fast
;; encoder with one that is slightly slower, but allows custom
;; things to be encoded:
(ns myns
  (:require [cheshire.core :refer :all]
            [cheshire.generate :refer [add-encoder encode-str remove-encoder]]))

;; First, add a custom encoder for a class:
(add-encoder java.awt.Color
             (fn [c jsonGenerator]
               (.writeString jsonGenerator (str c))))

;; There are also helpers for common encoding actions:
(add-encoder java.net.URL encode-str)

;; List of common encoders that can be used: (see generate.clj)
;; encode-nil
;; encode-number
;; encode-seq
;; encode-date
;; encode-bool
;; encode-named
;; encode-map
;; encode-symbol
;; encode-ratio

;; Then you can use encode from the custom namespace as normal
(encode (java.awt.Color. 1 2 3))
;; => "java.awt.Color[r=1,g=2,b=3]"

;; Custom encoders can also be removed:
(remove-encoder java.awt.Color)

;; Decoding remains the same, you are responsible for doing custom decoding.
```

<h3>NOTE: `cheshire.custom` has been deprecated in version 5.0.0</h3>

Custom and Core encoding have been combined in Cheshire 5.0.0, so
there is no longer any need to require a different namespace depending
on what you would like to use.

### Aliases

There are also a few aliases for commonly used functions:

    encode -> generate-string
    encode-stream -> generate-stream
    encode-smile -> generate-smile
    decode -> parse-string
    decode-stream -> parse-stream
    decode-smile -> parse-smile

## Features
Cheshire supports encoding standard clojure datastructures, with a few
additions.

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
- numbers (Integer, Long, BigInteger, BigInt, Double, Float, Ratio,
  Short, Byte, primitives)
- clojure.lang.PersistentQueue

### Java classes
- Date
- UUID
- java.sql.Timestamp
- any java.util.Set
- any java.util.Map
- any java.util.List

### Custom class encoding while still being fast

### Also supports
- Stream encoding/decoding
- Lazy decoding
- Pretty-printing JSON generation
- Unicode escaping
- Custom keyword coercion
- Arbitrary precision for decoded values:

Cheshire will automatically use a BigInteger if needed for
non-floating-point numbers, however, for floating-point numbers,
Doubles will be used unless the `*use-bigdecimals?*` symbol is bound
to true:

```clojure
(ns foo.bar
  (require [cheshire.core :as json]
           [cheshire.parse :as parse]))

(json/decode "111111111111111111111111111111111.111111111111111111111111111111111111")
;; => 1.1111111111111112E32 (a Double)

(binding [parse/*use-bigdecimals?* true]
  (json/decode "111111111111111111111111111111111.111111111111111111111111111111111111"))
;; => 111111111111111111111111111111111.111111111111111111111111111111111111M (a BigDecimal)
```

- Replacing default encoders for builtin types
- [SMILE encoding/decoding](http://wiki.fasterxml.com/SmileFormatSpec)

## Change Log

[Change log](https://github.com/dakrone/cheshire/blob/master/ChangeLog.md) is available on GitHub.

## Speed

Cheshire is about twice as fast as data.json.

Check out the benchmarks in `cheshire.test.benchmark`; or run `lein
benchmark`. If you have scenarios where Cheshire is not performing as
well as expected (compared to a different library), please let me
know.

## Experimental things

In the `cheshire.experimental` namespace:

```
$ echo "Hi. \"THIS\" is a string.\\yep." > /tmp/foo

$ lein repl
user> (use 'cheshire.experimental)
nil
user> (use 'clojure.java.io)
nil
user> (println (slurp (encode-large-field-in-map {:id "10"
                                                  :things [1 2 3]
                                                  :body "I'll be removed"}
                                                 :body
                                                 (input-stream (file "/tmp/foo")))))
{"things":[1,2,3],"id":"10","body":"Hi. \"THIS\" is a string.\\yep.\n"}
nil
```

`encode-large-field-in-map` is used for streamy JSON encoding where
you want to JSON encode a map, but don't want the map in memory all at
once (it returns a stream). Check out the docstring for full usage.

It's experimental, like the name says. Based on [Tigris](http://github.com/dakrone/tigris).

## Advanced customization for factories
See
[this](http://fasterxml.github.com/jackson-core/javadoc/2.1.1/com/fasterxml/jackson/core/JsonFactory.Feature.html)
and
[this](http://fasterxml.github.com/jackson-core/javadoc/2.1.1/com/fasterxml/jackson/core/JsonParser.Feature.html)
for a list of features that can be customized if desired. A custom
factory can be used like so:

```clojure
(ns myns
  (:require [cheshire.core :as core]
            [cheshire.factory :as factory]))

(binding [factory/*json-factory* (factory/make-json-factory
                                  {:allow-non-numeric-numbers true})]
  (json/decode "{\"foo\":NaN}" true))))))
```

See the `default-factory-options` map in
[factory.clj](https://github.com/dakrone/cheshire/blob/master/src/cheshire/factory.clj)
for a full list of configurable options. Smile factories can also be
created, and factories work exactly the same with custom encoding.

## Future Ideas/TODOs
- <del>move away from using Java entirely, use Protocols for the
  custom encoder</del> (see custom.clj)
- <del>allow custom encoders</del> (see custom.clj)
- <del>figure out a way to encode namespace-qualified keywords</del>
- <del>look into overriding the default encoding handlers with custom handlers</del>
- <del>better handling when java numbers overflow ECMAScript's numbers
  (-2^31 to (2^31 - 1))</del>
- <del>handle encoding java.sql.Timestamp the same as
  java.util.Date</del>
- <del>add benchmarking</del>
- get criterium benchmarking ignored for 1.2.1 profile
- <del> look into faster exception handling by pre-allocating an exception
  object instead of creating one on-the-fly (maybe ask Steve?)</del>
- make it as fast as possible (ongoing)

## License
Release under the MIT license. See LICENSE for the full license.

## Thanks
Thanks go to Mark McGranaghan for clj-json and Jim Duey for the name
suggestion. :)
