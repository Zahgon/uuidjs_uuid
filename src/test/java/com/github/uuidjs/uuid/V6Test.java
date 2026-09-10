package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/v6.test.ts}. */
@DisplayName("v6")
class V6Test {

  private static final String V1_ID = "f1207660-21d2-11ef-8c4f-419efbd44d48";
  private static final String V6_ID = "1ef21d2f-1207-6660-8c4f-419efbd44d48";

  private static final long FULL_MSECS = 0x133b891f705L;
  private static final int FULL_NSECS = 0x1538;
  private static final int FULL_CLOCKSEQ = 0x385c;
  private static final byte[] FULL_NODE = {
    (byte) 0x61, (byte) 0xcd, (byte) 0x3c, (byte) 0xbb, (byte) 0x32, (byte) 0x10
  };

  private static Version1Options fullOptions() {
    return new Version1Options()
        .msecs(FULL_MSECS)
        .nsecs(FULL_NSECS)
        .clockseq(FULL_CLOCKSEQ)
        .node(FULL_NODE.clone());
  }

  private static final byte[] EXPECTED_BYTES = {
    (byte) 0x1e,
    (byte) 0x11,
    (byte) 0x22,
    (byte) 0xbd,
    (byte) 0x94,
    (byte) 0x28,
    (byte) 0x68,
    (byte) 0x88,
    (byte) 0xb8,
    (byte) 0x5c,
    (byte) 0x61,
    (byte) 0xcd,
    (byte) 0x3c,
    (byte) 0xbb,
    (byte) 0x32,
    (byte) 0x10,
  };

  @Test
  @DisplayName("default behavior")
  void defaultBehavior() {
    // Verify explicit options produce expected id
    String id = V6.v6();
    assertTrue(
        Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-6[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
            .matcher(id)
            .find(),
        "id is valid v6 UUID");
  }

  @Test
  @DisplayName("default behavior (binary type)")
  void defaultBehaviorBinaryType() {
    byte[] buffer = new byte[16];
    byte[] result = V6.v6(fullOptions(), buffer);
    assertArrayEquals(EXPECTED_BYTES, buffer);
    assertSame(buffer, result);
  }

  @Test
  @DisplayName("all options")
  void allOptions() {
    // Verify explicit options produce expected id
    String id = V6.v6(fullOptions());
    assertEquals("1e1122bd-9428-6888-b85c-61cd3cbb3210", id);
  }

  @Test
  @DisplayName("sort by creation time")
  void sortByCreationTime() {
    // Verify ids sort by creation time
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ids.add(V6.v6(new Version1Options().msecs((long) i * 1000)));
    }
    List<String> sorted = new ArrayList<>(ids);
    sorted.sort(null);
    assertEquals(sorted, ids);
  }

  @Test
  @DisplayName("sort by creation time (time_low overflow)")
  void sortByCreationTimeTimeLowOverflow() {
    // The timestamp is assembled from a low and a high half, and `nsecs` can
    // carry from one into the other. See "v1 sort order (time_low overflow)".
    long msecs = 1321645585614L;
    List<String> ids = new ArrayList<>();
    for (int nsecs : new int[] {4382, 4383, 4384, 4385}) {
      ids.add(V6.v6(new Version1Options().msecs(msecs).nsecs(nsecs)));
    }
    List<String> sorted = new ArrayList<>(ids);
    sorted.sort(null);
    assertEquals(sorted, ids);
  }

  @Test
  @DisplayName("creating at array offset")
  void creatingAtArrayOffset() {
    byte[] buffer = new byte[32];
    V6.v6(fullOptions(), buffer, 0);
    V6.v6(fullOptions(), buffer, 16);

    byte[] expectedBuf = new byte[32];
    System.arraycopy(EXPECTED_BYTES, 0, expectedBuf, 0, 16);
    System.arraycopy(EXPECTED_BYTES, 0, expectedBuf, 16, 16);

    assertArrayEquals(expectedBuf, buffer);
  }

  @Test
  @DisplayName("throws RangeError for out-of-range indexes")
  void throwsRangeErrorForOutOfRangeIndexes() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V6.v6(new Version1Options(), buf15));
    assertThrows(JSRangeError.class, () -> V6.v6(new Version1Options(), buf30, -1));
    assertThrows(JSRangeError.class, () -> V6.v6(new Version1Options(), buf30, 15));
  }

  @Test
  @DisplayName("random node has multicast bit set")
  void randomNodeHasMulticastBitSet() {
    // https://www.rfc-editor.org/rfc/rfc9562.html#section-6.10-3
    for (int i = 0; i < 100; i++) {
      assertTrue((Parse.parse(V6.v6())[10] & 0x01) != 0, "v6() node multicast bit");
      assertTrue(
          (Parse.parse(V6.v6(new Version1Options().msecs(FULL_MSECS)))[10] & 0x01) != 0,
          "v6({msecs}) node multicast bit");
    }
  }

  @Test
  @DisplayName("v1 -> v6 conversion")
  void v1ToV6Conversion() {
    String id = V1ToV6.v1ToV6(V1_ID);
    assertEquals(V6_ID, id);
  }

  @Test
  @DisplayName("v6 -> v1 conversion")
  void v6ToV1Conversion() {
    String id = V6ToV1.v6ToV1(V6_ID);
    assertEquals(V1_ID, id);
  }
}
