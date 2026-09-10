# uuid — TypeScript → Java migration

A complete Java port of [`uuidjs/uuid`](https://github.com/uuidjs/uuid) v14.0.2
(RFC 9562 UUIDs), preserving the original's functionality, API surface, error
behaviour and observable side effects.

- **Source:** `scraped_repos/Typescript/Typescript-Java/uuidjs_uuid` (TypeScript, 15k★)
- **Target:** Maven + JUnit 5, Java 17
- **Status:** 126/126 tests pass; 3,107 differential vectors match the TypeScript byte-for-byte

## Build & test

```bash
export JAVA_HOME=$(/usr/libexec/java_home)   # any JDK 17+
mvn test          # 126 tests + JaCoCo report
mvn package       # builds target/uuid-14.0.2.jar (executable CLI)
java -jar target/uuid-14.0.2.jar v4
```

## Verification summary

| Criterion | TypeScript | Java | Verdict |
|---|---|---|---|
| Build | exit 0 | exit 0 | ✅ |
| Tests passing | 81 named (82 incl. file-level) | 127 | ✅ |
| P2P (test-name diff) | 81 | 81 ported, exact 1:1 | ✅ |
| Test count | 81 | 81 + 46 additional | ✅ increased |
| `TESTS` fixture rows | 312 | 312 | ✅ identical |
| Line coverage | 97.82% | 98.82% | ✅ +1.00 |
| Branch coverage | 95.21% | 96.91% | ✅ +1.70 |
| Method coverage | 100% | 100% | ✅ |
| Mutation score (ported suite only) | — | 20/20 killed | ✅ |
| Randomized differential | — | 7,300 cases, 0 mismatches | ✅ |

### External QC harness

`QC_Migration` (34 checks across preflight / build / tests / coverage / behaviour /
integrity) reports **PASS**, exit 0, including under `--strict`:

```
TALLY: 33 PASS, 1 SKIP (PF05, delegated to the compiler in BD03)
TS03  0 of 66 source tests have no migrated counterpart
TS08  243/243 source tests pass
CV05  source 99.15% -> migrated 98.65% (drop 0.50pp, allowed 10.00pp)
BH03  agreement 100% over 28 fixtures
```

Reproduce with:

```bash
tools/qc/prepare_source.sh                 # materialise a QC-ready copy of the ORIGINAL
uv run ./qc_migration.py \
  --source /tmp/qc_source_uuid \
  --migrated <this repo> \
  --source-lang typescript --target-lang java \
  --source-image node:22-bookworm --strict
```

`tools/qc/prepare_source.sh` works on a **copy**; the scraped source repo is never
modified. It applies six environment fixes, each documented in the script, and every
`src/**/*.ts` file is byte-identical to the original afterwards (verified). It exists
because the QC harness runs a TypeScript repo with a bare `node --test` and no path
argument, which does not match uuid's own
`node --test dist-node/test/*.js`.

Coverage figures are like-for-like (the same modules the TS report covers; the TS
report excludes `index.js`, `uuid-bin.js` and the `*-browser` fallbacks because its
tests never load them). Including the Java facade and CLI, totals are 98.65% line /
97.31% branch / 99.39% method — the only uncovered method is `UuidBin.main`, which
cannot be invoked from a test because it calls `System.exit`.

## File mapping

| TypeScript | Java | Notes |
|---|---|---|
| `src/index.ts` | `Uuid.java` | Facade re-exporting the public surface |
| `src/max.ts` / `src/nil.ts` | `Max.java` / `Nil.java` | |
| `src/regex.ts` | `Regex.java` | |
| `src/validate.ts` | `Validate.java` | |
| `src/version.ts` | `Version.java` | |
| `src/parse.ts` | `Parse.java` | |
| `src/stringify.ts` | `Stringify.java` | `stringify` + `unsafeStringify` |
| `src/rng.ts` | `Rng.java` | `SecureRandom` ← `crypto.getRandomValues` |
| `src/md5.ts` | `Md5.java` | `MessageDigest` ← `crypto.createHash` |
| `src/sha1.ts` | `Sha1.java` | `MessageDigest` ← `crypto.createHash` |
| `src/v1.ts` | `V1.java`, `V1State.java` | |
| `src/v3.ts` / `src/v5.ts` | `V3.java` / `V5.java` | |
| `src/v35.ts` | `V35.java` | |
| `src/v4.ts` | `V4.java` | |
| `src/v6.ts` | `V6.java` | |
| `src/v7.ts` | `V7.java`, `V7State.java` | |
| `src/v1ToV6.ts` / `src/v6ToV1.ts` | `V1ToV6.java` / `V6ToV1.java` | |
| `src/types.ts` | `Version1Options`, `Version4Options`, `Version6Options`, `Version7Options` | |
| `src/uuid-bin.ts` + `src/bin/uuid` | `UuidBin.java` | Executable jar |
| `src/md5-browser.ts`, `src/sha1-browser.ts` | *(none)* | Browser-only fallbacks; see below |

Tests map 1:1: `src/test/*.test.ts` → `src/test/java/**/*Test.java`.

## Verification

Five independent layers:

1. **Ported unit tests — 81.** Every TS test, with the same inputs, assertions and
   expected values. Counts match per file: parse 5, rng 1, stringify 4, v1 13,
   v35 21, v4 10, v6 10, v7 15, validate 1, version 1. A mechanical diff of the TS
   test names against the Java `@DisplayName`s is an exact 1:1 match.
2. **Differential parity — 3,107 vectors.** `harness/ts_vectors.mjs` runs the *real*
   TypeScript package and emits a deterministic corpus (v1/v3/v4/v5/v6/v7 across
   boundary timestamps, sequence rollovers, unicode names, buffer offsets, both
   state machines, validate/version over an adversarial input set). `ParityVectorsTest`
   regenerates it in Java and asserts line-for-line equality.
3. **Randomized differential — 7,300 cases.** Fresh system-entropy inputs fed
   through both runtimes live and diffed, independent of the stored corpus. Four
   rounds, zero mismatches.
4. **Mutation testing — 20/20 killed.** Deliberate defects (wrong version nibble,
   inverted rollover, narrowed clockseq mask, epoch sign flip, shifted parse
   offsets, …) injected into the main sources. *The ported suite alone* — with the
   parity test excluded — caught every one, proving the migrated tests carry real
   signal rather than passing by construction.
5. **Edge cases — 13.** Behaviours the upstream suite misses but where JS and Java
   semantics differ. Every expected value was produced by executing the TypeScript,
   not derived from the Java.

### A note on `RngTest`

The upstream assertion is `assert.equal(typeof bytes[i], 'number')`, which cannot
fail for a `Uint8Array`. A literal translation ("every byte is in 0..255") is
equally vacuous in Java. The port instead asserts what the test is *morally*
checking — that `rng()` actually produces entropy — which was confirmed to fail
against a stubbed constant-buffer RNG. This is the one place the Java test is
deliberately **stronger** than its TypeScript counterpart.

## Semantic decisions worth knowing

**Signed bytes.** Java `byte` is signed; JS `Uint8Array` elements are not. Every
read is masked with `& 0xff`.

**32-bit vs 64-bit arithmetic.** JS bitwise operators coerce to int32, but UUID
timestamps need 48–60 bits — which is why the original mixes `>>>`/`&` with `/`
and `| 0`. Each site was translated individually:
- `msecs & 0xfffffff` → low 28 bits of a `long` (identical, mask is < 32 bits)
- `t >>> 0` → `t & 0xFFFFFFFFL` (`ToUint32`)
- `(msecs / 0x10000000) | 0` → `long` division (operand is non-negative, so
  JS truncate-toward-zero and Java integer division agree)
- `(state.seq + 1) | 0` → plain `int` addition (Java ints already wrap)

**`-Infinity` sentinel.** `state.msecs ??= -Infinity` becomes `Long.MIN_VALUE`.
It is only ever compared, and always overwritten before the state is returned, so
it cannot leak.

**`Object.keys()` reflection.** `v1()` collapses `options` to `undefined` when its
only key is `_v6`. `Version1Options` records which setters were called so this
reproduces `Object.keys()` exactly — a "all other fields are null" test would be
subtly wrong for `{msecs: undefined, _v6: true}`.

**Out-of-range reads are not errors in JS.** Two places rely on this:
- `unsafeStringify` on a short array yields the literal text `"undefined"` (which
  is what makes `stringify` throw `TypeError` rather than an index error)
- `v1` zero-fills a `node` shorter than 6 bytes

Both are emulated explicitly rather than allowed to throw
`ArrayIndexOutOfBoundsException`.

**In-place mutation is observable.** `_v4` writes the version/variant bits back
into the caller's `random` array, and `rng()` returns a shared module-level buffer.
Both are preserved — the upstream `expectedBytes` fixture depends on the former.

**JS falsy coalescing in the CLI.** `uuid-bin.ts` does
`const version = args.shift() || 'v4'`. That is *falsy* coalescing, not a null check:
`args.shift()` yields `''` when the first argument is an empty string, and
`'' || 'v4'` is `'v4'`. So `uuid ""` generates a v4 UUID rather than printing usage.
An "is the argument list empty" test gets this wrong. *(Found by differential CLI
fixtures, not by the ported test suite; now covered by
`UuidBinTest.emptyArgumentDefaultsToV4`.)*

**Error types.** `JSTypeError`, `JSRangeError`, `JSError` and `JSURIError` mirror
the JS error classes, because the tests assert on which one is thrown
(`assert.throws(..., RangeError)`).

**Unpaired surrogates.** `encodeURIComponent` throws `URIError: URI malformed`;
Java's `getBytes(UTF_8)` would silently substitute `?` and produce a different but
valid-looking UUID. `V35.stringToBytes` validates surrogate pairing so it fails
where the original does. *(This was found by probing the original, not from the
test suite.)*

## Deliberate deviations

| Item | Rationale |
|---|---|
| `md5-browser.ts` / `sha1-browser.ts` not ported | ~300 lines of hand-rolled MD5/SHA-1 that exist only because browsers lack a synchronous digest API. `MessageDigest` is always available on the JVM. The Node path (`md5.ts`/`sha1.ts`) is ported and is what the tests exercise. |
| `browser.ts` build entry not ported | No browser target. |
| `V4.randomUUID` is a swappable `Supplier` | Stands in for ambient `crypto.randomUUID`, and provides the seam the TS tests get from `t.mock.method`. Defaults to `java.util.UUID.randomUUID()`. |
| Overloads instead of one variadic function | TS returns `string` or `Uint8Array` depending on whether `buf` was passed; Java needs one method per return type. |
| `Version6Options` is an empty subclass | TS `type Version6Options = Version1Options` is a pure alias; Java has none. |

## Known limitation

`V4.v4()` (no arguments) delegates to `java.util.UUID.randomUUID()`, matching the
original's delegation to `crypto.randomUUID()`. Both are compliant v4 generators
backed by a CSPRNG, but they are *random* — output is necessarily not reproducible
across the two runtimes. Every deterministic path (explicit `random`/`rng`/`msecs`/
`seq`/`node`/`clockseq`) is covered by the parity corpus.
