package com.github.uuidjs.uuid;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Port of {@code src/v1.ts} — time-based UUIDs.
 *
 * <p>Inspired by https://github.com/LiosK/UUID.js and
 * http://docs.python.org/library/uuid.html
 */
public final class V1 {
  private V1() {}

  /** Mirrors the module-level {@code const _state: V1State = {}}. */
  private static final V1State STATE = new V1State();

  /** Offset to Gregorian epoch, per RFC 9562 §5.1. */
  private static final long GREGORIAN_OFFSET_MS = 12219292800000L;

  // ---------------------------------------------------------------------------
  // Public overloads. TypeScript expresses these as a single function whose
  // return type depends on whether `buf` was supplied; Java needs one method per
  // return type.
  // ---------------------------------------------------------------------------

  public static String v1() {
    return v1((Version1Options) null);
  }

  public static String v1(Version1Options options) {
    byte[] bytes = generate(options, null, 0);
    return Stringify.unsafeStringify(bytes, 0);
  }

  public static byte[] v1(Version1Options options, byte[] buf) {
    return v1(options, buf, 0);
  }

  public static byte[] v1(Version1Options options, byte[] buf, int offset) {
    return generate(options, buf, offset);
  }

  /**
   * Mirrors the body of the TS {@code v1()} implementation signature, including the
   * {@code _v6} extraction dance.
   */
  static byte[] generate(Version1Options options, byte[] buf, int offset) {
    byte[] bytes;

    // Extract _v6 flag from options, clearing options if appropriate
    boolean isV6 = options != null && options.isV6();
    if (options != null) {
      Set<String> optionsKeys = options.keys();
      if (optionsKeys.size() == 1 && optionsKeys.contains("_v6")) {
        options = null;
      }
    }

    if (options != null) {
      // With options: Make UUID independent of internal state
      bytes =
          v1Bytes(
              coalesceRandom(options.random(), options.rng()),
              options.msecs(),
              options.nsecs(),
              options.clockseq(),
              options.node(),
              buf,
              offset);
    } else {
      // Without options: Make UUID from internal state
      long now = System.currentTimeMillis();
      byte[] rnds = Rng.rng();

      updateV1State(STATE, now, rnds);

      // Generate UUID. Note that v6 uses random values for `clockseq` and `node`.
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-5.6-4
      bytes =
          v1Bytes(
              rnds,
              STATE.msecs,
              STATE.nsecs,
              isV6 ? null : STATE.clockseq,
              isV6 ? null : STATE.node,
              buf,
              offset);
    }

    return bytes;
  }

  /** Mirrors {@code options.random ?? options.rng?.() ?? rng()}. */
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
   * <p>Mirrors {@code export function updateV1State(state, now, rnds)}.
   */
  public static V1State updateV1State(V1State state, long now, byte[] rnds) {
    if (state.msecs == null) {
      state.msecs = V1State.NEGATIVE_INFINITY;
    }
    if (state.nsecs == null) {
      state.nsecs = 0;
    }

    // Update timestamp
    if (now == state.msecs) {
      // Same msec-interval = simulate higher clock resolution by bumping `nsecs`
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.1-2.6
      state.nsecs = state.nsecs + 1;

      // Check for `nsecs` overflow (nsecs is capped at 10K intervals / msec)
      if (state.nsecs >= 10000) {
        // Prior to uuid@11 this would throw an error, however the RFCs allow for
        // changing the node in this case. This slightly breaks monotonicity at
        // msec granularity, but that's not a significant concern.
        // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.1-2.16
        state.node = null;
        state.nsecs = 0;
      }
    } else if (now > state.msecs) {
      // Reset nsec counter when clock advances to a new msec interval
      state.nsecs = 0;
    } else if (now < state.msecs) {
      // Handle clock regression
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.1-2.7
      //
      // Note: Unsetting node here causes both it and clockseq to be randomized,
      // below.
      state.node = null;
    }

    // Init node and clock sequence (do this after timestamp update which may
    // reset the node) https://www.rfc-editor.org/rfc/rfc9562.html#section-5.1-7
    if (state.node == null) {
      state.node = Arrays.copyOfRange(rnds, 10, 16);

      // Set multicast bit
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.10-3
      state.node[0] |= 0x01;

      // Clock sequence must be randomized
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-5.1-8
      state.clockseq = (((rnds[8] & 0xff) << 8) | (rnds[9] & 0xff)) & 0x3fff;
    }

    state.msecs = now;

    return state;
  }

  /**
   * Mirrors the private {@code v1Bytes(...)}.
   *
   * <p><b>Numeric fidelity notes.</b>
   *
   * <ul>
   *   <li>{@code msecs & 0xfffffff} in JS applies {@code ToInt32} then masks to 28
   *       bits. Since the mask is narrower than 32 bits, the result is simply the low
   *       28 bits of {@code msecs} — identical to the Java {@code long} mask.
   *   <li>{@code t >>> 0} is {@code ToUint32}, i.e. {@code t mod 2^32}, written here
   *       as {@code t & 0xFFFFFFFFL}.
   *   <li>{@code (msecs / 0x10000000) | 0} is float division followed by
   *       truncation-toward-zero. {@code msecs} is always positive here, so Java's
   *       integer division is equivalent.
   * </ul>
   */
  static byte[] v1Bytes(
      byte[] rnds,
      Long msecsIn,
      Integer nsecsIn,
      Integer clockseqIn,
      byte[] nodeIn,
      byte[] buf,
      int offset) {

    if (rnds.length < 16) {
      throw new JSError("Random bytes length must be >= 16");
    }

    // Defaults
    if (buf == null) {
      buf = new byte[16];
      offset = 0;
    } else {
      if (offset < 0 || offset + 16 > buf.length) {
        throw new JSRangeError(
            "UUID byte range " + offset + ":" + (offset + 15) + " is out of buffer bounds");
      }
    }

    long msecs = (msecsIn == null) ? System.currentTimeMillis() : msecsIn;
    int nsecs = (nsecsIn == null) ? 0 : nsecsIn;
    int clockseq =
        (clockseqIn == null)
            ? ((((rnds[8] & 0xff) << 8) | (rnds[9] & 0xff)) & 0x3fff)
            : clockseqIn;
    byte[] node = nodeIn;
    if (node == null) {
      node = Arrays.copyOfRange(rnds, 10, 16);

      // Set multicast bit
      // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.10-3
      node[0] |= 0x01;
    }

    // Offset to Gregorian epoch
    // https://www.rfc-editor.org/rfc/rfc9562.html#section-5.1-1
    msecs += GREGORIAN_OFFSET_MS;

    // The timestamp is `msecs * 10000 + nsecs`, which needs more precision than a
    // JS number has, so it is computed as a 32-bit low half and a 28-bit high
    // half. `msecs` is split at bit 28, because `0x10000000 * 10000` is exactly
    // `625 * 0x100000000`: its high bits then contribute only to the high half of
    // the timestamp, and its low bits only to the low half.
    long t = (msecs & 0xfffffffL) * 10000 + nsecs;

    // `time_low`
    long tl = t & 0xFFFFFFFFL;
    buf[offset++] = (byte) ((tl >>> 24) & 0xff);
    buf[offset++] = (byte) ((tl >>> 16) & 0xff);
    buf[offset++] = (byte) ((tl >>> 8) & 0xff);
    buf[offset++] = (byte) (tl & 0xff);

    // `time_mid`. Note that adding `nsecs`, above, may have carried out of
    // `time_low`, so `t`'s own high bits have to be folded in here.
    long tmh = ((msecs / 0x10000000L) * 625 + (t / 0x100000000L)) & 0xfffffffL;
    buf[offset++] = (byte) ((tmh >>> 8) & 0xff);
    buf[offset++] = (byte) (tmh & 0xff);

    // `time_high_and_version`
    buf[offset++] = (byte) (((tmh >>> 24) & 0xf) | 0x10); // include version
    buf[offset++] = (byte) ((tmh >>> 16) & 0xff);

    // `clock_seq_hi_and_reserved` | variant
    buf[offset++] = (byte) ((clockseq >>> 8) | 0x80);

    // `clock_seq_low`
    buf[offset++] = (byte) (clockseq & 0xff);

    // `node`
    //
    // NOTE: a caller-supplied `node` shorter than 6 bytes is not an error in the
    // original. Reading past the end of a JS typed array yields `undefined`, and
    // assigning `undefined` into a Uint8Array stores 0. e.g.
    // `v1({node: Uint8Array.of(1, 2), ...})` ends `...-010200000000`. Java would
    // throw ArrayIndexOutOfBoundsException, so the out-of-range read is emulated.
    for (int n = 0; n < 6; ++n) {
      buf[offset++] = (n < node.length) ? node[n] : 0;
    }

    return buf;
  }
}
