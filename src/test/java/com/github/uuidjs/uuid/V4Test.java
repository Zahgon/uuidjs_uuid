package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/v4.test.ts}. */
@DisplayName("v4")
class V4Test {

  private static byte[] randomBytesFixture() {
    return new byte[] {
      (byte) 0x10,
      (byte) 0x91,
      (byte) 0x56,
      (byte) 0xbe,
      (byte) 0xc4,
      (byte) 0xfb,
      (byte) 0xc1,
      (byte) 0xea,
      (byte) 0x71,
      (byte) 0xb4,
      (byte) 0xef,
      (byte) 0xe1,
      (byte) 0x67,
      (byte) 0x1c,
      (byte) 0x58,
      (byte) 0x36,
    };
  }

  private static final byte[] EXPECTED_BYTES = {
    (byte) 16,
    (byte) 145,
    (byte) 86,
    (byte) 190,
    (byte) 196,
    (byte) 251,
    (byte) 65,
    (byte) 234,
    (byte) 177,
    (byte) 180,
    (byte) 239,
    (byte) 225,
    (byte) 103,
    (byte) 28,
    (byte) 88,
    (byte) 54,
  };

  @Test
  @DisplayName("subsequent UUIDs are different")
  void subsequentUuidsAreDifferent() {
    String id1 = V4.v4();
    String id2 = V4.v4();

    assertNotEquals(id1, id2);
  }

  /**
   * Port note: the TS test uses {@code t.mock.method(crypto, 'randomUUID', ...)} to
   * count calls to the platform generator. The Java equivalent swaps the {@link
   * V4#randomUUID} supplier, which is the seam that stands in for the ambient
   * {@code crypto.randomUUID}.
   */
  @Test
  @DisplayName("should use native randomUUID() if no option is passed")
  void shouldUseNativeRandomUuid() {
    Supplier<String> original = V4.randomUUID;
    AtomicInteger callCount = new AtomicInteger(0);
    V4.randomUUID =
        () -> {
          callCount.incrementAndGet();
          return "mocked-uuid";
        };
    try {
      assertEquals(0, callCount.get());
      V4.v4();
      assertEquals(1, callCount.get());
    } finally {
      V4.randomUUID = original;
    }
  }

  @Test
  @DisplayName("should not use native randomUUID() if an option is passed")
  void shouldNotUseNativeRandomUuid() {
    Supplier<String> original = V4.randomUUID;
    AtomicInteger callCount = new AtomicInteger(0);
    V4.randomUUID =
        () -> {
          callCount.incrementAndGet();
          return "mocked-uuid";
        };
    try {
      assertEquals(0, callCount.get());
      V4.v4(new Version4Options());
      assertEquals(0, callCount.get());
    } finally {
      V4.randomUUID = original;
    }
  }

  @Test
  @DisplayName("explicit options.random produces expected result")
  void explicitOptionsRandom() {
    String id = V4.v4(new Version4Options().random(randomBytesFixture()));
    assertEquals("109156be-c4fb-41ea-b1b4-efe1671c5836", id);
  }

  @Test
  @DisplayName("explicit options.rng produces expected result")
  void explicitOptionsRng() {
    String id = V4.v4(new Version4Options().rng(V4Test::randomBytesFixture));
    assertEquals("109156be-c4fb-41ea-b1b4-efe1671c5836", id);
  }

  @Test
  @DisplayName("fills one UUID into a buffer as expected")
  void fillsOneUuid() {
    byte[] buffer = new byte[16];
    byte[] result = V4.v4(new Version4Options().random(randomBytesFixture()), buffer);

    assertArrayEquals(EXPECTED_BYTES, buffer);
    assertSame(buffer, result);
  }

  @Test
  @DisplayName("fills two UUIDs into a buffer as expected")
  void fillsTwoUuids() {
    byte[] buffer = new byte[32];
    V4.v4(new Version4Options().random(randomBytesFixture()), buffer, 0);
    V4.v4(new Version4Options().random(randomBytesFixture()), buffer, 16);

    byte[] expectedBuf = new byte[32];
    System.arraycopy(EXPECTED_BYTES, 0, expectedBuf, 0, 16);
    System.arraycopy(EXPECTED_BYTES, 0, expectedBuf, 16, 16);

    assertArrayEquals(expectedBuf, buffer);
  }

  @Test
  @DisplayName("throws when option.random is too short")
  void throwsWhenRandomTooShort() {
    byte[] random = {16}; // length = 1
    byte[] buffer = new byte[16];
    assertThrows(JSError.class, () -> V4.v4(new Version4Options().random(random), buffer));
  }

  @Test
  @DisplayName("throws when options.rng() is too short")
  void throwsWhenRngTooShort() {
    byte[] buffer = new byte[16];
    assertThrows(
        JSError.class, () -> V4.v4(new Version4Options().rng(() -> new byte[] {0}), buffer));
  }

  @Test
  @DisplayName("throws RangeError for out-of-range indexes")
  void throwsRangeErrorForOutOfRangeIndexes() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V4.v4(new Version4Options(), buf15));
    assertThrows(JSRangeError.class, () -> V4.v4(new Version4Options(), buf30, -1));
    assertThrows(JSRangeError.class, () -> V4.v4(new Version4Options(), buf30, 15));
  }
}
