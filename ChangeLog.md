## Changes between Cheshire 5.13.0 and 5.13.1

* Bump minimum JDK version from v7 to v8
* Bump Jackson dependencies to 2.18.3
* Expose new Jackson processing limits via factory options
* Internal maintenance: migrate away from usages of deprecated Jackson 

## Changes between Cheshire 5.8.1 and 5.9.0

* Add `parse-stream-strict` to parse streams strictly rather than lazily
* Bump Jackson dependencies to 2.9.9
* Chunk size for lazy seqs is now configurable

## Changes between Cheshire 5.8.0 and 5.8.1

* Add `:quote-field-names` parameter to control quoting field names in encoding

## Changes between Cheshire 5.7.2 and 5.8.0

* Fix type hints for un-imported classes
* Update Jackson dependencies to mitigate vulnerability https://github.com/FasterXML/jackson-databind/issues/1599
* Correct spelling in documentation
* Add `cheshire.exact` namespace for exactly-once decoding
* Bump Jackson dependencies to 2.9.0

## Changes between Cheshire 5.6.3 and 5.6.2

* Fix float coercion when encoding

## Changes between Cheshire 5.6.2 and 5.6.1

* Fix type hints for newer clojure version
* Bump Jackson dependencies

## Changes between Cheshire 5.6.1 and 5.6.0

* Fix javac target for 1.6 compatibility

## Changes between Cheshire 5.6.0 and 5.5.0

* Fixes for type hinting
* Make :pretty option configurable to use custom pretty printer

## Changes between Cheshire 5.5.0 and 5.5.0

* Bump Jackson dependencies

## Changes between Cheshire 5.4.0 and 5.3.2

* Add CBOR encoding/decoding
* Add docstrings for clojure-json aliases
* Add default encoder for java.lang.Character
* Add sequential write support
* Bump dependencies

## Changes between Cheshire 5.3.1 and 5.3.0

* Fix string parsing for 1 and 2 arity methods
* Bump Jackson to 2.3.1

## Changes between Cheshire 5.3.0 and 5.2.0

* Dependencies have been bumped
* Parse streams strictly by default to avoid scoping issues

## Changes between Cheshire 5.2.0 and 5.1.2

* Bump tigris to 0.1.1 to use PushbackReader
* Lazily decode top-level arrays (thanks ztellman)

## Changes between Cheshire 5.1.2 and 5.1.1

* Add experimental namespace
* Bump Jackson deps to 2.2.1

## Changes between Cheshire 5.1.1 and 5.1.0

* Remove all reflection (thanks amalloy)
* Fixed custom encoder helpers (thanks lynaghk)

## Changes between Cheshire 5.1.0 and 5.0.2

* Allow custom keyword function for encoding (thanks goodwink)

## Changes between Cheshire 5.0.2 and 5.0.1

* Bump Jackson dependency from 2.1.1 to 2.1.3

* Add more type hinting (thanks to ztellman)

## Changes between Cheshire 5.0.0 and 5.0.1

* Protocol custom encoders now take precedence over regular map
  encoders.

* Benchmarking is now a separate lein command, not a test selector.

## Changes between Cheshire 5.0.0 and 4.0.x

### Custom Encoders Changes

Custom encoder functions were moved to the `cheshire.generate` namespace:

 * `cheshire.custom/add-encoder` is now `cheshire.generate/add-encoder`
 * `cheshire.custom/remove-encoder` is now `cheshire.generate/remove-encoder`

In addition, `cheshire.custom/encode` and `cheshire.custom/decode` are no longer
necessary. Use `cheshire.core/encode` and `cheshire.core/decode` instead and
those functions will pick up custom encoders while still preserving the same
level of efficiency.

GH issue: [#32](https://github.com/dakrone/cheshire/issues/32).
