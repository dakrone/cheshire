<!-- 
The changelog is currently manually updated

Changes should always be included when potentially interesting to users.

Each change should include:
- if possible/practical/meaningful, link to issue (else link to PR else link to commit)
- short description of change
- link to github profile of author of change

Place any breaking changes under a "BREAKING changes" list item 
-->

# v6.1.0 - UNRELEASED

* Add encoding for `java.time.Instant`
  [#241](https://github.com/dakrone/cheshire/pull/241)
  ([@mthl](https://github.com/mthl))

# v6.0.0 - 2025-04-15

* **BREAKING** changes 
  * [#217](https://github.com/dakrone/cheshire/issues/217):
    Windows only: pretty printing now consistently uses JVM's OS default of `\r\n` for `:line-break`
    ([@lread](https://github.com/lread))
* Bump minimum JDK version from v7 to v8
  ([@lread](https://github.com/lread))
* Bump Jackson to v2.18.3
  ([@lread](https://github.com/lread))
* [#209](https://github.com/dakrone/cheshire/pull/209):
  Fix type hints on `generate-stream`
  ([@souenzzo](https://github.com/souenzzo)])
* [#210](https://github.com/dakrone/cheshire/issues/210):
  Expose new Jackson processing limits via factory options
  ([@lread](https://github.com/lread))
* [#215](https://github.com/dakrone/cheshire/issues/215):
  Add `:escape-non-ascii` to factory options
  ([@lread](https://github.com/lread))
* Internal maintenance: 
  * [#212](https://github.com/dakrone/cheshire/issues/212):
    Migrate away from usages of deprecated Jackson 
    ([@lread](https://github.com/lread))
* Quality
  * [#114](https://github.com/dakrone/cheshire/issues/114):
    Freshen changelog and add missing history
    ([@lread](https://github.com/lread))
  * [#214](https://github.com/dakrone/cheshire/issues/214):
    Add CI on GitHub Actions and with test coverage on Linux and Windows
    ([@lread](https://github.com/lread))
  * [#222](https://github.com/dakrone/cheshire/issues/222):
    Add linting
    ([@lread](https://github.com/lread))
  * [#226](https://github.com/dakrone/cheshire/issues/226):
    Add vulnerability scanning for cheshire dependencies
    ([@lread](https://github.com/lread))

[commit log](https://github.com/dakrone/cheshire/compare/5.13.0...6.0.0)

# v5.13.0 - 2024-04-01

* Bump Jackson to v2.17.0
  ([@antonmos](https://github.com/antonmos))

[commit log](https://github.com/dakrone/cheshire/compare/5.12.0...5.13.0)

# v5.12.0 - 2023-09-19

* Bump Jackson to v2.15.2
  ([@jakepearson](https://github.com/jakepearson))

[commit log](https://github.com/dakrone/cheshire/compare/5.11.0...5.12.0)

# v5.11.0 - 2022-05-26

* Bump Jackson to v2.13.3
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.10.2...5.11.0)

# v5.10.2 - 2022-01-24

* [ab52d71](https://github.com/dakrone/cheshire/commit/ab52d71):
  Add data reader `#cheshire/json` tag to generate JSON strings
  ([@mrroman](https://github.com/mrroman))
* [#187](https://github.com/dakrone/cheshire/pull/187):
  Exclude jackson-data-bind dependency, cheshire doesn't use and it has triggered many CVEs
  ([@technomancy](https://github.com/technomancy))

[commit log](https://github.com/dakrone/cheshire/compare/5.10.1...5.10.2)

# v5.10.1 - 2021-07-13

* Bump Jackson to v2.12.4
  ([@dakrone](https://github.com/dakrone))
* [#170](https://github.com/dakrone/cheshire/pull/170):
  Add `:strict-duplicate-detection` to factory options
  ([@sjamaan](https://github.com/sjamaan))
* [7040a4a](https://github.com/dakrone/cheshire/commit/7040a4a):
  Optimization: replace internal usage of `doseq` with `reduce`
  ([@nilern](https://github.com/nilern))

[commit log](https://github.com/dakrone/cheshire/compare/5.10.0...5.10.1)

# v5.10.0 - 2020-02-04

* Bump Jackson to v2.10.2
  ([@willcohen](https://github.com/willcohen))
* [#150](https://github.com/dakrone/cheshire/pull/150):
  Bump tigris to v0.1.2
  ([@aiba](https://github.com/aiba))
* [9d69b18](https://github.com/dakrone/cheshire/commit/9d69b18):
  Fix misplaced docstring (found with clj-kondo)
  ([@borkdude](https://github.com/borkdude))

[commit log](https://github.com/dakrone/cheshire/compare/5.9.0...5.10.0)

# v5.9.0 - 2019-08-05

* Bump Jackson to v2.9.9
  ([@kumarshantanu](https://github.com/kumarshantanu))
* [b980d98](https://github.com/dakrone/cheshire/commit/b980d98):
  Add `parse-stream-strict` to parse streams strictly rather than lazily
  ([@nilern](https://github.com/nilern))
* [#141](https://github.com/dakrone/cheshire/pull/141):
  Chunk size for lazy seqs is now configurable
  ([@johnswanson](https://github.com/johnswanson) & ([@cayennes](https://github.com/cayennes)))
* [fe453ea](https://github.com/dakrone/cheshire/commit/fe453ea):
  Fix misplaced docstring (found with clj-kondo)
  ([@borkdude](https://github.com/borkdude))

[commit log](https://github.com/dakrone/cheshire/compare/5.8.1...5.9.0)

# v5.8.1 - 2018-09-21

* Bump Jackson to v2.9.6
  ([@ikitommi](https://github.com/ikitommi))
* [4ceaf16](https://github.com/dakrone/cheshire/commit/4ceaf16):
  Add `:quote-field-names` factory option to control quoting field names in encoding
  ([@crazymerlyn](https://github.com/crazymerlyn))
* [#106](https://github.com/dakrone/cheshire/issues/106):
  Respect `:indent-objects?` `false` in pretty printer
  ([@crazymerlyn](https://github.com/crazymerlyn))
* [#128](https://github.com/dakrone/cheshire/issues/128):
  Add dependency on tools.namespace for clojure 1.9 compatability
  ([@dakrone](https://github.com/dakrone))
* [#131](https://github.com/dakrone/cheshire/issues/131):
  Fix type hint in `parse-string-strict`
  ([@spieden](https://github.com/spieden))

[commit log](https://github.com/dakrone/cheshire/compare/5.8.0...5.8.1)

# v5.8.0 - 2017-08-15 

* Bump Jackson to v2.9.0
  ([@brabster](https://github.com/brabster))
* [#111](https://github.com/dakrone/cheshire/issues/112):
  Fix type hints for un-imported classes
  ([@dakrone](https://github.com/dakrone))
* Add `cheshire.exact` namespace for exactly-once decoding
  ([@lucacervello](https://github.com/lucacervello))
* Add only valid json option when parsing json
  ([@lucacervello](https://github.com/lucacervello))

[commit log](https://github.com/dakrone/cheshire/compare/5.7.1...5.8.0)

# v5.7.1 - 2017-04-20

* [#112](https://github.com/dakrone/cheshire/issues/112):
  Remove `condp` usage in `generate` to avoid memory issues for lazy seqs
  ([@senior](https://github.com/senior))

[commit log](https://github.com/dakrone/cheshire/compare/5.7.0...5.7.1)

# v5.7.0 - 2017-01-13

* Bump Jackson to v2.8.6
  ([@dakrone](https://github.com/dakrone))
* [847077d](https://github.com/dakrone/cheshire/commit/847077d):
  Allow non const booleans `decode` for `key-fn` 
  ([@bfabry](https://github.com/bfabry))

[commit log](https://github.com/dakrone/cheshire/compare/5.6.3...5.7.0)

# v5.6.3 - 2016-06-27

* [#97](https://github.com/dakrone/cheshire/issues/97):
  Fix float coercion when encoding
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.6.2...5.6.3)

# v5.6.2 - 2016-06-20

* Bump Jackson to v2.7.5
  ([@dakrone](https://github.com/dakrone))
* [e949e00](https://github.com/dakrone/cheshire/commit/e949e00)
  Fix type hints for newer clojure version
  ([@pjstadig](https://github.com/pjstadig))

[commit log](https://github.com/dakrone/cheshire/compare/5.6.1...5.6.2)

# v5.6.1 - 2016-04-12

* [0d90f44](https://github.com/dakrone/cheshire/commit/0d90f44):
  Tell javac to generate Java 1.6 compatible class files
  ([@Deraen](https://github.com/Deraen))

[commit log](https://github.com/dakrone/cheshire/compare/5.6.0...5.6.1)

# v5.6.0 - 2016-04-10

* Bump Jackson to v2.7.3
  ([@niwinz](https://github.com/niwinz))
* [#96](https://github.com/dakrone/cheshire/pull/96)
  Fixes for type hinting
  ([@ahjones](https://github.com/ahjones))
* [77f9813](https://github.com/dakrone/cheshire/commit/77f9813)
  Support byte-array encoding/decoding
  ([@blendmaster](https://github.com/blendmaster))
* [#99](https://github.com/dakrone/cheshire/issues/99):
  Make `:pretty` option configurable to use custom pretty printer
  ([@prayerslayer](https://github.com/prayerslayer))

[commit log](https://github.com/dakrone/cheshire/compare/5.5.0...5.6.0)

# v5.5.0 - 2015-05-28

* Bump Jackson to v2.5.3
  ([@laczoka](https://github.com/laczoka))
* [7124611](https://github.com/dakrone/cheshire/commit/7124611):
  Add docstrings for clojure-json aliases
  ([@danielcompton](https://github.com/danielcompton))
* [#72](https://github.com/dakrone/cheshire/issues/72) 
  Address Jackson deprecations
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.4.0...5.5.0)

# v5.4.0 - 2014-12-10

* Bump Jackson to v2.4.4
  ([@dakrone](https://github.com/dakrone))
* [ffac717](https://github.com/dakrone/cheshire/commit/ffac717):
  Add CBOR encoding/decoding
  ([@dakrone](https://github.com/dakrone))
* [#53](https://github.com/dakrone/cheshire/issues/53): 
  Add default encoder for `java.lang.Character`
  ([@dakrone](https://github.com/dakrone))
* [56662c2](https://github.com/dakrone/cheshire/commit/56662c2):
  Add sequential write support
  ([@kostafey](https://github.com/kostafey))

[commit log](https://github.com/dakrone/cheshire/compare/5.3.1...5.4.0)

# v5.3.1 - 2014-01-10

* Bump Jackson to v2.3.1
  ([@dakrone](https://github.com/dakrone))
* [#46](https://github.com/dakrone/cheshire/issues/46) 
& [#48](https://github.com/dakrone/cheshire/issues/48):
  Fix string parsing for 1 and 2 arity methods
  ([@maxnoel](https://github.com/maxnoel))

[commit log](https://github.com/dakrone/cheshire/compare/5.3.0...5.3.1)

# v5.3.0 - 2013-12-18

* Bump Jackson to v2.3.0
  ([@dakrone](https://github.com/dakrone))
* [#48](https://github.com/dakrone/cheshire/issues/48):
  Parse streams strictly by default to avoid scoping issues
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.2.0...5.3.0)

# v5.2.0 - 2013-05-22

* Bump tigris to v0.1.1 to use `PushbackReader`
  ([@dakrone](https://github.com/dakrone))
* Performance tweaks
  ([@ztellman](https://github.com/ztellman))

[commit log](https://github.com/dakrone/cheshire/compare/5.1.2...5.2.0)

# v5.1.2 - 2013-05-17

* Bump Jackson to v2.2.1
  ([@dakrone](https://github.com/dakrone))
* [ce4ff056](https://github.com/dakrone/cheshire/commit/ce4ff056):
  Add experimental namespace
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.1.1...5.1.2)

# v5.1.1 - 2013-04-12

* [e4cc3b3](https://github.com/dakrone/cheshire/commit/e4cc3b3):
  Remove all reflection 
  ([@amalloy](https://github.com/amalloy))
* [6d78a56](https://github.com/dakrone/cheshire/commit/6d78a56):
  Fixed custom encoder helpers 
  ([@lynaghk](https://github.com/lynaghk))

[commit log](https://github.com/dakrone/cheshire/compare/5.1.0...5.1.1)

# v5.1.0 - 2013-04-04

* Bump Jackson to v2.1.4
  ([@dakrone](https://github.com/dakrone))
* [c5f0be3](https://github.com/dakrone/cheshire/commit/c5f0be3):
  Allow custom keyword function for encoding 
  ([@goodwink](https://github.com/goodwink))

[commit log](https://github.com/dakrone/cheshire/compare/5.0.2...5.1.0)

# v5.0.2 - 2013-02-19

* Bump Jackson to v2.1.3
  ([@dakrone](https://github.com/dakrone))
* [03aff88](https://github.com/dakrone/cheshire/commit/03aff88):
  Add more type hinting 
  ([@ztellman](https://github.com/ztellman))

[commit log](https://github.com/dakrone/cheshire/compare/5.0.1...5.0.2)

# v5.0.1 - 2012-12-04

* [#32](https://github.com/dakrone/cheshire/issues/32):
  Protocol custom encoders now take precedence over regular map encoders.
  ([@dakrone](https://github.com/dakrone))
* Benchmarking is now a separate lein command, not a test selector.
  ([@dakrone](https://github.com/dakrone))
* Performance tweaks
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/5.0.0...5.0.1)

# v5.0.0 - 2012-09-20

* [#32](https://github.com/dakrone/cheshire/issues/32):
  Custom Encoders Changes
  ([@dakrone](https://github.com/dakrone))
  * Custom encoder functions were moved to the `cheshire.generate` namespace:
    * `cheshire.custom/add-encoder` is now `cheshire.generate/add-encoder`
    * `cheshire.custom/remove-encoder` is now `cheshire.generate/remove-encoder`
  * In addition, `cheshire.custom/encode` and `cheshire.custom/decode` are no longer
necessary. Use `cheshire.core/encode` and `cheshire.core/decode` instead and
those functions will pick up custom encoders while still preserving the same
level of efficiency.
  * The `cheshire.custom` namespace is now deprecated 
* Bump Jackson to v2.1.1
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/4.0.4...5.0.0)

# v4.0.4 - 2012-11-19

* Bump Jackson to v2.1.0
  ([@dakrone](https://github.com/dakrone))
* [811ea45](https://github.com/dakrone/cheshire/commit/811ea45):
  Add missing writer arg for custom `encode-stream` passthrough to `encode-stream*`
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/4.0.3...4.0.4)

# v4.0.3 - 2012-09-22

* Bump Jackson to v2.0.6
  ([@dakrone](https://github.com/dakrone))
* [e7c5088](https://github.com/dakrone/cheshire/commit/e7c5088):
  Make clojure a dev dependency
  ([@dakrone](https://github.com/dakrone))
* [10062af](https://github.com/dakrone/cheshire/commit/10062af):
  Encode empty sets to empty arrays for consistency with empty array encoding
  ([@dlebrero](https://github.com/dlebrero))
* [9b32e2e](https://github.com/dakrone/cheshire/commit/9b32e2e):
  Fix custom encoding of namespaced keywords
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/4.0.2...4.0.3)

# v4.0.2 - 2012-08-30

* Bump Jackson to v2.0.5
  ([@dakrone](https://github.com/dakrone))
* [bacb3ac](https://github.com/dakrone/cheshire/commit/bacb3ac):
  Add support for escaping non-ASCII chars when decoding 
  ([@dakrone](https://github.com/dakrone))
* [52a700b](https://github.com/dakrone/cheshire/commit/52a700b):
  Add support for custom munging of keys when decoding
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/4.0.1...4.0.2)

# v4.0.1 - 2012-06-12

* Bump Jackson to v2.0.4
  ([@dakrone](https://github.com/dakrone))
* [6087d7f](https://github.com/dakrone/cheshire/commit/6087d7f):
  Add support for encoding `Byte` and `Short` to int
  ([@dakrone](https://github.com/dakrone))
* [d05acde](https://github.com/dakrone/cheshire/commit/d05acde):
  Add support for encoding `clojure.lang.Associative` to json map
  ([@warsus](https://github.com/warsus))
* Performance tweaks
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/4.0.0...4.0.1)

# v4.0.0 - 2012-04-13

* [20ebaa8](https://github.com/dakrone/cheshire/commit/20ebaa8):
  Add optional default pretty printer to json generator
  ([@drewr](https://github.com/drewr))
* Support encoding with custom date format 
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/3.1.0...4.0.0)

# v3.1.0 - 2012-03-30

* Bump Jackson to v2.0.0
  ([@dakrone](https://github.com/dakrone))
* Performance tweaks
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/3.0.0...3.1.0)

# v3.0.0 - 2012-03-09

* [65ace30](https://github.com/dakrone/cheshire/commit/65ace30):
  Add support for encoding `clojure.lang.PersitentQueue`
  ([@dakrone](https://github.com/dakrone))
* [89348a7](https://github.com/dakrone/cheshire/commit/89348a7):
  Throw `JsonGenerationExcption` instead of `Exception` when encoding fails
  ([@dakrone](https://github.com/dakrone))
* [714bec2](https://github.com/dakrone/cheshire/commit/714bec2):
  Custom encoding now falls back to core encoding by default
  ([@dakrone](https://github.com/dakrone))
* [263ed65](https://github.com/dakrone/cheshire/commit/263ed65):
  Encode qualified keywords `{:document/name "My document"} -> {"document/name" : "My document"}`
  ([@maxweber](https://github.com/maxweber))

[commit log](https://github.com/dakrone/cheshire/compare/2.2.2...3.0.0)

# v2.2.2 - 2012-03-06

* [6e4df40](https://github.com/dakrone/cheshire/commit/6e4df40):
  Generate symbols as strings, encoding non-resolvable symbol without throwing
  ([@dakrone](https://github.com/dakrone))
* [#19](https://github.com/dakrone/cheshire/issues/19): 
  Fix custom encoding for Longs
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.2.1...2.2.2)

# v2.2.1 - 2012-03-05

* Bump Jackson to v1.9.5
  ([@dakrone](https://github.com/dakrone))
* [d2a9ad8a](https://github.com/dakrone/cheshire/commit/d2a9ad8a):
  Add support for `java.util.{Map,List,Set}`
  ([@ndimiduk](https://github.com/ndimiduk))
* Performance tweaks
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.2.0...2.2.1)

# v2.2.0 - 2012-02-07

* [#16](https://github.com/dakrone/cheshire/issues/16): 
  Allow parsing floats into BigDecimal to retain precision 
  ([@dakrone](https://github.com/dakrone))
* Various performance tweaks
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.1.0...2.2.0)

# v2.1.0 - 2012-01-27

* Bump Jackson to v1.9.4
  ([@dakrone](https://github.com/dakrone))
* Introduce `*json-factory*`, `*smile-factory*`, `*cbor-factory*` as a way to specify options to Jackson
  ([@dakrone](https://github.com/dakrone))
* Address some reflection warnings
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.6...2.1.0)

# v2.0.6 - 2012-01-12

* [b96106e](https://github.com/dakrone/cheshire/commit/b96106e):
  Encode ns-qualified symbols consistently
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.5...2.0.6)

# v2.0.5 - 2012-01-11

* [eaee529](https://github.com/dakrone/cheshire/commit/eaee529):
  Encode symbols that are not resolvable as strings
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.4...2.0.5)

# v2.0.4 - 2011-11-29

* [434099d](https://github.com/dakrone/cheshire/commit/434099d):
  Support custom array coercion when parsing
  ([@sbtourist](https://github.com/sbtourist))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.3...2.0.4)

# v2.0.3 - 2011-11-26

* Bump Jackson to v1.9.2
  ([@dakrone](https://github.com/dakrone))
* Support encoding of `clojure.lang.Ratio`
  ([@dakrone](https://github.com/dakrone))
* [de849ce](https://github.com/dakrone/cheshire/commit/de849ce):
  Support encoding of `BigDecimal`
  ([@CmdrDats](https://github.com/CmdrDats))
* Don't attempt to parse `nil` things
  ([@dakrone](https://github.com/dakrone) and [@zk](https://github.com/zk)) 
* [750ae7c](https://github.com/dakrone/cheshire/commit/750ae7c):
  Improve error message when encoding fails 
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.2...2.0.3)

# v2.0.2 - 2011-09-16

* [db755c4](https://github.com/dakrone/cheshire/commit/db755c4):
  Support encoding of `BigInteger`
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.1...2.0.2)

# v2.0.1 - 2011-08-30

* Bump Jackson to v1.8.5
  ([@dakrone](https://github.com/dakrone))
* [#6](https://github.com/dakrone/cheshire/issues/6):
  Support encoding of `java.sql.Timestamp`
  ([@dakrone](https://github.com/dakrone))
* [8c686c2](https://github.com/dakrone/cheshire/commit/8c686c2):
  Write namespace when encoding qualified keywords
  ([@dakrone](https://github.com/dakrone))
* Quality
  * Automated testing on Travis CI
    ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/2.0.0...2.0.1)

# v2.0.0 - 2011-08-02

* Bump Jackson to v1.8.3
  ([@dakrone](https://github.com/dakrone))
* Introduce `cheshire.factory` namespace
  ([@dakrone](https://github.com/dakrone))
* Continue work on custom encoders
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/1.1.4...2.0.0)

# v1.1.4 - 2011-06-28

* Bump Jackson to v1.8.2
  ([@dakrone](https://github.com/dakrone))
* [85f195b](https://github.com/dakrone/cheshire/commit/85f195b):
  Add experimental custom encoders
  ([@dakrone](https://github.com/dakrone))
* [13db92a](https://github.com/dakrone/cheshire/commit/13db92a):
  Add convenience methods for adding/removing custom encoders
  ([@zk](https://github.com/zk))

[commit log](https://github.com/dakrone/cheshire/compare/1.1.3...1.1.4)

# v1.1.3 - 2011-05-25

* Bump Jackson to v1.8.1
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/da8c206a22afffa55d4023b38ba0f7e6d4d69bdd...1.1.3)

# v1.1.2 - 2011-04-29

* [da8c206](https://github.com/dakrone/cheshire/commit/da8c206):
  Support clojure v1.3
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/1.1.1...da8c206a22afffa55d4023b38ba0f7e6d4d69bdd)

# v1.1.1 - 2011-04-18

* [9cdc44c](https://github.com/dakrone/cheshire/commit/9cdc44c):
  Add cheshire puss to README :)
  ([@dakrone](https://github.com/dakrone))
* [344e984](https://github.com/dakrone/cheshire/commit/344e984):
  Stop using java for parsing 
  ([@hiredman](https://github.com/hiredman))
* Quality
  * Add time tests, address reflection warnings, and turf unused code
    ([@hiredman](https://github.com/hiredman))

[commit log](https://github.com/dakrone/cheshire/compare/1.1.0...1.1.1)

# v1.1.0 - 2011-03-29

First world-consumption release of cheshire!

* [04b2ce4](https://github.com/dakrone/cheshire/commit/04b2ce4):
  Add `Date` and `UUID` encoding
  ([@dakrone](https://github.com/dakrone))
* [1e21b7d](https://github.com/dakrone/cheshire/commit/1e21b7d):
  Add stream handling (encode/decode)
  ([@dakrone](https://github.com/dakrone))
* [215038d](https://github.com/dakrone/cheshire/commit/215038d):
  Add helpers for streams
  ([@dakrone](https://github.com/dakrone))
* Add docs and cleanup docstrings 
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/fca4fbb871ba43b3a00db90d96dd3bc95aa00174...1.1.0)

# v1.0.2 - 2011-03-25

* [fca4fbb](https://github.com/dakrone/cheshire/commit/fca4fbb):
  Support encoding `#{}` and clojure symbols
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/76186a84a68442919d4c7d79eafae9ab40e5b9a0...fca4fbb871ba43b3a00db90d96dd3bc95aa00174)

# v1.0.1 - 2011-03-25

* [76186a8](https://github.com/dakrone/cheshire/commit/76186a8):
  Add helpers for people coming from `clojure-json`
  ([@dakrone](https://github.com/dakrone))

[commit log](https://github.com/dakrone/cheshire/compare/33a0e24d7b63fd091afae20666d03ac68c829411...76186a84a68442919d4c7d79eafae9ab40e5b9a0)

# v1.0.0 - 2011-03-25

First release of cheshire!

[commit log](https://github.com/dakrone/cheshire/compare/74eb30ec1a2f94c044c6e46ea4454ec3ab0e2934...33a0e24d7b63fd091afae20666d03ac68c829411)
