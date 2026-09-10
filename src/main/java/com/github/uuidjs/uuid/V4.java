package com.github.uuidjs.uuid;

import java.util.function.Supplier;

/** Port of {@code src/v4.ts}. */
public final class V4 {
  private V4() {}

  /**
   * Stand-in for the ambient {@code crypto.randomUUID} used by the fast path:
   *
   * <pre>
   * if (!buf &amp;&amp; !options &amp;&amp; crypto.randomUUID) {
   *   return crypto.randomUUID();
   * }
   * </pre>
   *
   * <p>Exposed as a swappable {@link Supplier} because the TS test suite replaces it
   * with {@code t.mock.method(crypto, 'randomUUID', ...)} to assert that the fast
   * path is taken for {@code v4()} but not for {@code v4({})}. {@link
   * java.util.UUID#randomUUID()} is itself a compliant RFC 4122 v4 generator backed
   * by a cryptographically strong PRNG, so it is the correct default.
   */
  static Supplier<String> randomUUID = () -> java.util.UUID.randomUUID().toString();

  public static String v4() {
    return v4((Version4Options) null);
  }

  public static String v4(Version4Options options) {
    if (options == null) {
      // Fast path: no buf, no options -> native randomUUID()
      return randomUUID.get();
    }
    return Stringify.unsafeStringify(v4Bytes(options, null, 0), 0);
  }

  public static byte[] v4(Version4Options options, byte[] buf) {
    return v4(options, buf, 0);
  }

  public static byte[] v4(Version4Options options, byte[] buf, int offset) {
    return v4Bytes(options, buf, offset);
  }

  /** Mirrors the private {@code _v4(...)} tail-code function. */
  private static byte[] v4Bytes(Version4Options options, byte[] buf, int offset) {
    if (options == null) {
      options = new Version4Options();
    }

    byte[] rnds = coalesceRandom(options.random(), options.rng());
    if (rnds.length < 16) {
      throw new JSError("Random bytes length must be >= 16");
    }

    // Per 4.4, set bits for version and `clock_seq_hi_and_reserved`.
    //
    // NOTE: this mutates the caller's array in place when `options.random` was
    // supplied. That side effect is observable (and relied upon by the upstream
    // tests, whose `expectedBytes` fixture holds the post-mutation values), so it
    // is preserved rather than defensively copied.
    rnds[6] = (byte) ((rnds[6] & 0x0f) | 0x40);
    rnds[8] = (byte) ((rnds[8] & 0x3f) | 0x80);

    // Copy bytes to buffer, if provided
    if (buf != null) {
      if (offset < 0 || offset + 16 > buf.length) {
        throw new JSRangeError(
            "UUID byte range " + offset + ":" + (offset + 15) + " is out of buffer bounds");
      }

      for (int i = 0; i < 16; ++i) {
        buf[offset + i] = rnds[i];
      }

      return buf;
    }

    return rnds;
  }

  private static byte[] coalesceRandom(byte[] random, Supplier<byte[]> rng) {
    if (random != null) {
      return random;
    }
    if (rng != null) {
      byte[] r = rng.get();
      if (r != null) {
        return r;
      }
    }
    return Rng.rng();
  }
}
