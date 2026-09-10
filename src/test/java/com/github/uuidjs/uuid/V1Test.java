package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/v1.test.ts}. */
@DisplayName("v1")
class V1Test {

  /** Verify ordering of v1 ids created with explicit times. */
  private static final long TIME = 1321644961388L; // 2011-11-18 11:36:01.388-08:00

  /**
   * Fixture values for testing with the rfc v1 UUID example:
   * https://www.rfc-editor.org/rfc/rfc9562.html#name-example-of-a-uuidv1-value
   */
  private static final String RFC_V1 = "c232ab00-9414-11ec-b3c8-9f68deced846";

  private static final byte[] RFC_V1_BYTES = Parse.parse(RFC_V1);

  // `options` for producing the above RFC UUID
  private static final long RFC_MSECS = 0x17f22e279b0L;
  private static final int RFC_NSECS = 0;
  private static final int RFC_CLOCKSEQ = 0x33c8;
  private static final byte[] RFC_NODE = {
    (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
  };

  private static Version1Options rfcOptions() {
    return new Version1Options()
        .msecs(RFC_MSECS)
        .nsecs(RFC_NSECS)
        .clockseq(RFC_CLOCKSEQ)
        .node(RFC_NODE.clone());
  }

  // random bytes for producing the above RFC UUID
  private static final byte[] RFC_RANDOM = {
    // unused
    0, 0, 0, 0, 0, 0, 0, 0,
    // clock seq
    (byte) (RFC_CLOCKSEQ >> 8), (byte) (RFC_CLOCKSEQ & 0xff),
    // node
    (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
  };

  /** Compare v1 timestamp fields chronologically. */
  private static int compareV1TimeField(String a, String b) {
    String[] pa = a.split("-");
    String[] pb = b.split("-");
    String ka = pa[2] + pa[1] + pa[0];
    String kb = pb[2] + pb[1] + pb[0];
    return ka.compareTo(kb);
  }

  /**
   * A `msecs` value for which `msecs * 10000` lands within 10000 (100-nanosecond
   * intervals) of a `time_low` overflow, so that `nsecs` values >= CARRY_NSECS carry
   * out of `time_low` and into `time_mid`. Such a `msecs` occurs roughly every 430
   * seconds.
   */
  private static final long CARRY_TIME = 1321645585614L; // 2011-11-18 11:46:25.614-08:00

  private static final int CARRY_NSECS = 4384;

  /**
   * Extract the 60-bit RFC 9562 timestamp from a v1 UUID.
   * https://www.rfc-editor.org/rfc/rfc9562.html#section-5.1
   */
  private static BigInteger v1Timestamp(String uuid) {
    String[] parts = uuid.split("-");
    BigInteger timeLow = new BigInteger(parts[0], 16);
    BigInteger timeMid = new BigInteger(parts[1], 16);
    BigInteger timeHighAndVersion = new BigInteger(parts[2], 16);
    return timeHighAndVersion
        .and(BigInteger.valueOf(0x0fff))
        .shiftLeft(48)
        .or(timeMid.shiftLeft(32))
        .or(timeLow);
  }

  /**
   * The timestamp `v1Timestamp()` should find: 100-nanosecond intervals since the
   * Gregorian epoch.
   */
  private static BigInteger expectedTimestamp(long msecs, int nsecs) {
    return BigInteger.valueOf(msecs)
        .add(BigInteger.valueOf(12219292800000L))
        .multiply(BigInteger.valueOf(10000))
        .add(BigInteger.valueOf(nsecs));
  }

  @Test
  @DisplayName("v1 sort order (default)")
  void v1SortOrderDefault() {
    List<String> ids = Arrays.asList(V1.v1(), V1.v1(), V1.v1(), V1.v1(), V1.v1());

    List<String> sorted = new ArrayList<>(ids);
    sorted.sort(V1Test::compareV1TimeField);
    assertEquals(sorted, ids);
  }

  @Test
  @DisplayName("v1 sort order (time option)")
  void v1SortOrderTimeOption() {
    List<String> ids =
        Arrays.asList(
            V1.v1(new Version1Options().msecs(TIME - 10L * 3600 * 1000)),
            V1.v1(new Version1Options().msecs(TIME - 1)),
            V1.v1(new Version1Options().msecs(TIME)),
            V1.v1(new Version1Options().msecs(TIME + 1)),
            V1.v1(new Version1Options().msecs(TIME + 28L * 24 * 3600 * 1000)));

    List<String> sorted = new ArrayList<>(ids);
    sorted.sort(V1Test::compareV1TimeField);
    assertEquals(sorted, ids);
  }

  @Test
  @DisplayName("v1 timestamp carries nsecs into time_mid")
  void v1TimestampCarriesNsecs() {
    for (int nsecs : new int[] {0, CARRY_NSECS - 1, CARRY_NSECS, 9999}) {
      assertEquals(
          expectedTimestamp(CARRY_TIME, nsecs),
          v1Timestamp(V1.v1(new Version1Options().msecs(CARRY_TIME).nsecs(nsecs))),
          "msecs = " + CARRY_TIME + ", nsecs = " + nsecs);
    }
  }

  @Test
  @DisplayName("v1 sort order (time_low overflow)")
  void v1SortOrderTimeLowOverflow() {
    List<String> ids =
        Arrays.asList(
            V1.v1(new Version1Options().msecs(CARRY_TIME).nsecs(CARRY_NSECS - 2)),
            V1.v1(new Version1Options().msecs(CARRY_TIME).nsecs(CARRY_NSECS - 1)),
            V1.v1(new Version1Options().msecs(CARRY_TIME).nsecs(CARRY_NSECS)),
            V1.v1(new Version1Options().msecs(CARRY_TIME).nsecs(CARRY_NSECS + 1)));

    List<String> sorted = new ArrayList<>(ids);
    sorted.sort(V1Test::compareV1TimeField);
    assertEquals(sorted, ids);
  }

  @Test
  @DisplayName("v1(options)")
  void v1Options() {
    assertEquals(
        RFC_V1,
        V1.v1(new Version1Options().msecs(RFC_MSECS).random(RFC_RANDOM.clone())),
        "minimal options");
    assertEquals(RFC_V1, V1.v1(rfcOptions()), "full options");
  }

  @Test
  @DisplayName("v1(options) equality")
  void v1OptionsEquality() {
    assertNotEquals(
        V1.v1(new Version1Options().msecs(TIME)),
        V1.v1(new Version1Options().msecs(TIME)),
        "UUIDs with minimal options differ");
    assertEquals(V1.v1(rfcOptions()), V1.v1(rfcOptions()), "UUIDs with full options are identical");
  }

  @Test
  @DisplayName("fills one UUID into a buffer as expected")
  void fillsOneUuid() {
    byte[] buffer = new byte[16];
    byte[] result = V1.v1(rfcOptions(), buffer);
    assertArrayEquals(RFC_V1_BYTES, buffer);
    assertSame(buffer, result);
  }

  @Test
  @DisplayName("fills two UUIDs into a buffer as expected")
  void fillsTwoUuids() {
    byte[] buffer = new byte[32];
    V1.v1(rfcOptions(), buffer, 0);
    V1.v1(rfcOptions(), buffer, 16);

    byte[] expectedBuf = new byte[32];
    System.arraycopy(RFC_V1_BYTES, 0, expectedBuf, 0, 16);
    System.arraycopy(RFC_V1_BYTES, 0, expectedBuf, 16, 16);

    assertArrayEquals(expectedBuf, buffer);
  }

  @Test
  @DisplayName("v1() state transitions")
  void v1StateTransitions() {
    // Test fixture for internal state passed into updateV1State function
    final long preMsecs = 10;
    final int preNsecs = 20;
    final int preClockseq = 0x1234;
    final byte[] preNode = {
      (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc
    };

    // Note: The test code, below, passes RFC_RANDOM as the `rnds` argument for
    // convenience. This allows us to test that fields have been initialized from
    // the rnds argument by testing for RFC option values in the output state.

    record Case(String title, V1State state, long now, V1State expected) {}

    List<Case> tests =
        List.of(
            new Case(
                "initial state",
                new V1State(),
                10,
                new V1State(10L, 0, RFC_CLOCKSEQ, RFC_NODE.clone())),
            new Case(
                "same time interval",
                new V1State(preMsecs, preNsecs, preClockseq, preNode.clone()),
                preMsecs,
                new V1State(preMsecs, 21, preClockseq, preNode.clone())),
            new Case(
                "new time interval",
                new V1State(preMsecs, preNsecs, preClockseq, preNode.clone()),
                preMsecs + 1,
                new V1State(preMsecs + 1, 0, preClockseq, preNode.clone())),
            new Case(
                "same time interval (nsecs overflow)",
                new V1State(preMsecs, 9999, preClockseq, preNode.clone()),
                preMsecs,
                new V1State(preMsecs, 0, RFC_CLOCKSEQ, RFC_NODE.clone())),
            new Case(
                "time regression",
                new V1State(preMsecs, preNsecs, preClockseq, preNode.clone()),
                preMsecs - 1,
                new V1State(preMsecs - 1, preNsecs, RFC_CLOCKSEQ, RFC_NODE.clone())));

    for (Case c : tests) {
      assertEquals(
          c.expected(), V1.updateV1State(c.state(), c.now(), RFC_RANDOM), "Failed: " + c.title());
    }
  }

  @Test
  @DisplayName("random node has multicast bit set")
  void randomNodeHasMulticastBitSet() {
    // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.10-3
    for (int i = 0; i < 100; i++) {
      assertTrue((Parse.parse(V1.v1())[10] & 0x01) != 0, "v1() node multicast bit");
      assertTrue(
          (Parse.parse(V1.v1(new Version1Options().msecs(TIME)))[10] & 0x01) != 0,
          "v1(options) node multicast bit");
    }
  }

  @Test
  @DisplayName("throws when option.random is too short")
  void throwsWhenRandomTooShort() {
    byte[] random = {16}; // length = 1
    byte[] buffer = new byte[16];
    assertThrows(JSError.class, () -> V1.v1(new Version1Options().random(random), buffer));
  }

  @Test
  @DisplayName("throws when options.rng() is too short")
  void throwsWhenRngTooShort() {
    byte[] buffer = new byte[16];
    assertThrows(
        JSError.class, () -> V1.v1(new Version1Options().rng(() -> new byte[] {0}), buffer));
  }

  @Test
  @DisplayName("throws RangeError for out-of-range indexes")
  void throwsRangeErrorForOutOfRangeIndexes() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V1.v1(new Version1Options(), buf15));
    assertThrows(JSRangeError.class, () -> V1.v1(new Version1Options(), buf30, -1));
    assertThrows(JSRangeError.class, () -> V1.v1(new Version1Options(), buf30, 15));
  }
}
