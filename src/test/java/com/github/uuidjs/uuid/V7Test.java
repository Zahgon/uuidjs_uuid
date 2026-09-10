package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/v7.test.ts}. */
@DisplayName("v7")
class V7Test {

  /**
   * Fixture values for testing with the rfc v7 UUID example:
   * https://www.rfc-editor.org/rfc/rfc9562.html#name-example-of-a-uuidv7-value
   */
  private static final String RFC_V7 = "017f22e2-79b0-7cc3-98c4-dc0c0c07398f";

  private static final byte[] RFC_V7_BYTES = Parse.parse("017f22e2-79b0-7cc3-98c4-dc0c0c07398f");
  private static final long RFC_MSECS = 0x17f22e279b0L;

  /** {@code option.seq} for the above RFC uuid. */
  private static final int RFC_SEQ = (0x0cc3 << 20) | (0x98c4dc >> 2);

  /** {@code option.random} for the above RFC uuid. */
  private static byte[] rfcRandom() {
    return new byte[] {
      (byte) 0x10,
      (byte) 0x91,
      (byte) 0x56,
      (byte) 0xbe,
      (byte) 0xc4,
      (byte) 0xfb,
      (byte) 0x0c,
      (byte) 0xc3,
      (byte) 0x18,
      (byte) 0xc4,
      (byte) 0x6c,
      (byte) 0x0c,
      (byte) 0x0c,
      (byte) 0x07,
      (byte) 0x39,
      (byte) 0x8f,
    };
  }

  @Test
  @DisplayName("subsequent UUIDs are different")
  void subsequentUuidsAreDifferent() {
    String id1 = V7.v7();
    String id2 = V7.v7();
    assertNotEquals(id1, id2);
  }

  @Test
  @DisplayName("explicit options.random and options.msecs produces expected result")
  void explicitRandomAndMsecs() {
    String id =
        V7.v7(new Version7Options().random(rfcRandom()).msecs(RFC_MSECS).seq(RFC_SEQ));
    assertEquals(RFC_V7, id);
  }

  @Test
  @DisplayName("explicit options.rng produces expected result")
  void explicitRng() {
    String id =
        V7.v7(new Version7Options().rng(V7Test::rfcRandom).msecs(RFC_MSECS).seq(RFC_SEQ));
    assertEquals(RFC_V7, id);
  }

  @Test
  @DisplayName("explicit options.msecs produces expected result")
  void explicitMsecs() {
    String id = V7.v7(new Version7Options().msecs(RFC_MSECS));
    assertEquals(0, id.indexOf("017f22e2"));
  }

  @Test
  @DisplayName("fills one UUID into a buffer as expected")
  void fillsOneUuid() {
    byte[] buffer = new byte[16];
    byte[] result =
        V7.v7(new Version7Options().random(rfcRandom()).msecs(RFC_MSECS).seq(RFC_SEQ), buffer);
    Stringify.stringify(buffer);

    assertArrayEquals(RFC_V7_BYTES, buffer);
    assertSame(buffer, result);
  }

  @Test
  @DisplayName("fills two UUIDs into a buffer as expected")
  void fillsTwoUuids() {
    byte[] buffer = new byte[32];

    V7.v7(new Version7Options().random(rfcRandom()).msecs(RFC_MSECS).seq(RFC_SEQ), buffer, 0);
    V7.v7(new Version7Options().random(rfcRandom()).msecs(RFC_MSECS).seq(RFC_SEQ), buffer, 16);

    byte[] expected = new byte[32];
    System.arraycopy(RFC_V7_BYTES, 0, expected, 0, 16);
    System.arraycopy(RFC_V7_BYTES, 0, expected, 16, 16);
    assertArrayEquals(expected, buffer);
  }

  //
  // monotonic and lexicographical sorting tests
  //

  @Test
  @DisplayName("lexicographical sorting is preserved")
  void lexicographicalSortingIsPreserved() {
    String id;
    String prior = null;
    long msecs = RFC_MSECS;
    for (int i = 0; i < 20000; ++i) {
      if (i % 1500 == 0) {
        // every 1500 runs increment msecs so seq is
        // reinitialized, simulating passage of time
        msecs += 1;
      }

      id = V7.v7(new Version7Options().msecs(msecs).seq(i));

      if (prior != null) {
        final String p = prior;
        final String c = id;
        assertTrue(p.compareTo(c) < 0, () -> p + " < " + c);
      }

      prior = id;
    }
  }

  @Test
  @DisplayName("can supply seq")
  void canSupplySeq() {
    int seq = 0x12345;
    String uuid = V7.v7(new Version7Options().msecs(RFC_MSECS).seq(seq));

    assertEquals("017f22e2-79b0-7000-848d-1", uuid.substring(0, 25));

    seq = 0x6fffffff;
    uuid = V7.v7(new Version7Options().msecs(RFC_MSECS).seq(seq));

    assertEquals("017f22e2-79b0-76ff-bfff-f", uuid.substring(0, 25));
  }

  @Test
  @DisplayName("internal seq is reset upon timestamp change")
  void internalSeqIsResetUponTimestampChange() {
    V7.v7(new Version7Options().msecs(RFC_MSECS).seq(0x6fffffff));

    String uuid = V7.v7(new Version7Options().msecs(RFC_MSECS + 1));

    assertNotEquals(15, uuid.indexOf("fff"));
  }

  @Test
  @DisplayName("v7() state transitions")
  void v7StateTransitions() {
    record Case(String title, V7State state, long now, V7State expected) {}

    List<Case> tests =
        List.of(
            new Case(
                "new time interval",
                new V7State(1L, 123),
                2,
                // time interval should update; sequence should be randomized
                new V7State(2L, 0xcc318c4)),
            new Case(
                "same time interval",
                new V7State(1L, 123),
                1,
                // timestamp unchanged; sequence increments
                new V7State(1L, 124)),
            new Case(
                "same time interval (sequence rollover)",
                new V7State(1L, 0xffffffff),
                1,
                // timestamp increments; sequence rolls over
                new V7State(2L, 0)),
            new Case(
                "time regression",
                new V7State(2L, 123),
                1,
                // timestamp unchanged; sequence increments
                new V7State(2L, 124)),
            new Case(
                "time regression (sequence rollover)",
                new V7State(2L, 0xffffffff),
                1,
                // timestamp increments (crazy, right? The system clock goes backwards
                // but the UUID timestamp moves forward? Weird, but it's what's
                // required to maintain monotonicity... and this is why we have unit
                // tests!)
                new V7State(3L, 0)));

    for (Case c : tests) {
      assertEquals(
          c.expected(), V7.updateV7State(c.state(), c.now(), rfcRandom()), "Failed: " + c.title());
    }
  }

  @Test
  @DisplayName("flipping bits changes the result")
  void flippingBitsChangesTheResult() {
    byte[] buf = new byte[16];
    BigInteger data = asBigInt(V7.v7(new Version7Options(), buf));
    String id = Stringify.stringify(buf);
    List<Integer> reserved = List.of(48, 49, 50, 51, 64, 65);
    for (int i = 0; i < 128; ++i) {
      if (reserved.contains(i)) {
        continue; // skip bits used for version and variant
      }
      BigInteger flipped = flip(data, i);
      final int bit = i;
      assertEquals(
          flipped.toString(16),
          asBigInt(V7.v7(optionsFrom(flipped), buf)).toString(16),
          () -> "Unequal uuids at bit " + bit);
      assertNotEquals(id, Stringify.stringify(buf));
    }
  }

  /** Convert a 16-byte array to a BigInteger (big-endian, unsigned). */
  private static BigInteger asBigInt(byte[] buf) {
    BigInteger acc = BigInteger.ZERO;
    for (byte b : buf) {
      acc = acc.shiftLeft(8).or(BigInteger.valueOf(b & 0xff));
    }
    return acc;
  }

  /** Mirrors {@code BigInt.asUintN(bits, data)} followed by {@code Number(...)}. */
  private static long asNumber(int bits, BigInteger data) {
    return data.and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE)).longValueExact();
  }

  /** Flip the nth bit (big-endian) in a BigInteger. */
  private static BigInteger flip(BigInteger data, int n) {
    return data.xor(BigInteger.ONE.shiftLeft(127 - n));
  }

  /** Extract v7 {@code options} from a (BigInteger) UUID. */
  private static Version7Options optionsFrom(BigInteger data) {
    long ms = asNumber(48, data.shiftRight(80));
    long hi = asNumber(12, data.shiftRight(64));
    long lo = asNumber(20, data.shiftRight(42));
    BigInteger r = data.and(BigInteger.ONE.shiftLeft(42).subtract(BigInteger.ONE));

    // NOTE: `(hi << 20) | lo` is 32-bit signed arithmetic in JS, so this is a plain
    // Java int expression (which may legitimately be negative).
    int seq = ((int) hi << 20) | (int) lo;

    byte[] random = new byte[16];
    // Array(6).fill(0).map((_, i) => asNumber(8, r >> (i * 8))).reverse()
    for (int i = 0; i < 6; i++) {
      long b = asNumber(8, r.shiftRight(i * 8));
      random[10 + (5 - i)] = (byte) b;
    }

    return new Version7Options().msecs(ms).seq(seq).random(random);
  }

  @Test
  @DisplayName("throws when option.random is too short")
  void throwsWhenRandomTooShort() {
    byte[] random = {16}; // length = 1
    byte[] buffer = new byte[16];
    assertThrows(JSError.class, () -> V7.v7(new Version7Options().random(random), buffer));
  }

  @Test
  @DisplayName("throws when options.rng() is too short")
  void throwsWhenRngTooShort() {
    byte[] buffer = new byte[16];
    assertThrows(
        JSError.class, () -> V7.v7(new Version7Options().rng(() -> new byte[] {0}), buffer));
  }

  @Test
  @DisplayName("throws RangeError for out-of-range indexes")
  void throwsRangeErrorForOutOfRangeIndexes() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V7.v7(new Version7Options(), buf15));
    assertThrows(JSRangeError.class, () -> V7.v7(new Version7Options(), buf30, -1));
    assertThrows(JSRangeError.class, () -> V7.v7(new Version7Options(), buf30, 15));
  }

  @Test
  @DisplayName("default seq (no explicit seq option) is consistent with updateV7State formula")
  void defaultSeqIsConsistentWithUpdateV7State() {
    // When v7() is called with random bytes and msecs but no explicit seq, the
    // default seq should use the same formula as updateV7State() -
    // ((rnds[6] & 0x7f) << 24) | (rnds[7] << 16) | (rnds[8] << 8) | rnds[9].
    //
    // Regression: the formula was `(rnds[6] * 0x7f) << 24`, which should have
    // been `(rnds[6] & 0x7f) << 24`, producing a seq inconsistent with
    // updateV7State.
    byte[] rnds = {
      (byte) 0x02,
      (byte) 0x91,
      (byte) 0x56,
      (byte) 0xbe,
      (byte) 0xc4,
      (byte) 0xfb,
      (byte) 0x02,
      (byte) 0xc3,
      (byte) 0x18,
      (byte) 0xc4,
      (byte) 0x6c,
      (byte) 0x0c,
      (byte) 0x0c,
      (byte) 0x07,
      (byte) 0x39,
      (byte) 0x8f,
    };
    V7State state = V7.updateV7State(new V7State(), RFC_MSECS, rnds);
    String uuidWithExplicitSeq =
        V7.v7(new Version7Options().random(rnds).msecs(RFC_MSECS).seq(state.seq()));
    String uuidWithDefaultSeq = V7.v7(new Version7Options().random(rnds).msecs(RFC_MSECS));
    assertEquals(uuidWithExplicitSeq, uuidWithDefaultSeq);
  }
}
