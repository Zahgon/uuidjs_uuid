package com.github.uuidjs.uuid;

import java.util.function.Supplier;

/** Port of {@code src/v7.ts}. */
public final class V7 {
  private V7() {}

  /** Mirrors the module-level {@code const _state: V7State = {}}. */
  private static final V7State STATE = new V7State();

  public static String v7() {
    return v7((Version7Options) null);
  }

  public static String v7(Version7Options options) {
    return Stringify.unsafeStringify(generate(options, null, 0), 0);
  }

  public static byte[] v7(Version7Options options, byte[] buf) {
    return v7(options, buf, 0);
  }

  public static byte[] v7(Version7Options options, byte[] buf, int offset) {
    return generate(options, buf, offset);
  }

  private static byte[] generate(Version7Options options, byte[] buf, int offset) {
    if (options != null) {
      // With options: Make UUID independent of internal state
      return v7Bytes(
          coalesceRandom(options.random(), options.rng()),
          options.msecs(),
          options.seq(),
          buf,
          offset);
    }

    // No options: Use internal state
    long now = System.currentTimeMillis();
    byte[] rnds = Rng.rng();

    updateV7State(STATE, now, rnds);

    return v7Bytes(rnds, STATE.msecs, STATE.seq, buf, offset);
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

  /**
   * (Private!) Do not use. This method is only exported for testing purposes and may
   * change without notice.
   *
   * <p>Mirrors {@code export function updateV7State(state, now, rnds)}. The TS
   * {@code state.seq = (state.seq + 1) | 0} is plain {@code int} addition here — Java
   * ints already wrap at 32 bits.
   */
  public static V7State updateV7State(V7State state, long now, byte[] rnds) {
    if (state.msecs == null) {
      state.msecs = V7State.NEGATIVE_INFINITY;
    }
    if (state.seq == null) {
      state.seq = 0;
    }

    if (now > state.msecs) {
      // Time has moved on! Pick a new random sequence number
      state.seq = v7Sequence(rnds);
      state.msecs = now;
    } else {
      // Bump sequence counter w/ 32-bit rollover
      state.seq = state.seq + 1;

      // In case of rollover, bump timestamp to preserve monotonicity. This is
      // allowed by the RFC and should self-correct as the system clock catches
      // up. See https://www.rfc-editor.org/rfc/rfc9562.html#section-6.2-9.4
      if (state.seq == 0) {
        state.msecs = state.msecs + 1;
      }
    }

    return state;
  }

  /**
   * Mirrors the private {@code v7Bytes(...)}.
   *
   * <p>The six timestamp bytes use {@code /} rather than {@code >>>} in the original,
   * because a 48-bit millisecond value exceeds the 32-bit range of JS bitwise
   * operators. {@code msecs} is non-negative, so JS float-division-then-truncate is
   * equivalent to Java {@code long} division.
   */
  static byte[] v7Bytes(byte[] rnds, Long msecsIn, Integer seqIn, byte[] buf, int offset) {
    if (rnds.length < 16) {
      throw new JSError("Random bytes length must be >= 16");
    }

    if (buf == null) {
      buf = new byte[16];
      offset = 0;
    } else {
      if (offset < 0 || offset + 16 > buf.length) {
        throw new JSRangeError(
            "UUID byte range " + offset + ":" + (offset + 15) + " is out of buffer bounds");
      }
    }

    // Defaults
    long msecs = (msecsIn == null) ? System.currentTimeMillis() : msecsIn;
    int seq = (seqIn == null) ? v7Sequence(rnds) : seqIn;

    // byte 0-5: timestamp (48 bits)
    buf[offset++] = (byte) ((msecs / 0x10000000000L) & 0xff);
    buf[offset++] = (byte) ((msecs / 0x100000000L) & 0xff);
    buf[offset++] = (byte) ((msecs / 0x1000000L) & 0xff);
    buf[offset++] = (byte) ((msecs / 0x10000L) & 0xff);
    buf[offset++] = (byte) ((msecs / 0x100L) & 0xff);
    buf[offset++] = (byte) (msecs & 0xff);

    // byte 6: `version` (4 bits) | sequence bits 28-31 (4 bits)
    buf[offset++] = (byte) (0x70 | ((seq >>> 28) & 0x0f));

    // byte 7: sequence bits 20-27 (8 bits)
    buf[offset++] = (byte) ((seq >>> 20) & 0xff);

    // byte 8: `variant` (2 bits) | sequence bits 14-19 (6 bits)
    buf[offset++] = (byte) (0x80 | ((seq >>> 14) & 0x3f));

    // byte 9: sequence bits 6-13 (8 bits)
    buf[offset++] = (byte) ((seq >>> 6) & 0xff);

    // byte 10: sequence bits 0-5 (6 bits) | random (2 bits)
    buf[offset++] = (byte) (((seq << 2) & 0xff) | (rnds[10] & 0x03));

    // bytes 11-15: random (40 bits)
    buf[offset++] = rnds[11];
    buf[offset++] = rnds[12];
    buf[offset++] = rnds[13];
    buf[offset++] = rnds[14];
    buf[offset++] = rnds[15];

    return buf;
  }

  /** Mirrors {@code v7Sequence(rnds)}. */
  private static int v7Sequence(byte[] rnds) {
    return ((rnds[6] & 0x7f) << 24)
        | ((rnds[7] & 0xff) << 16)
        | ((rnds[8] & 0xff) << 8)
        | (rnds[9] & 0xff);
  }
}
