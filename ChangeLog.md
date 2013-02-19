## Changes between Cheshire 5.0.2 and 5.0.1

* Bump Jackson dependency from 5.1.1 to 5.1.3

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
