## Changes between Cheshire 5.0.0 and 5.0.1

No changes yet.


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
