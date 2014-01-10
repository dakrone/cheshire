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
