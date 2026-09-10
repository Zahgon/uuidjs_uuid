package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/stringify.test.ts}. */
@DisplayName("stringify")
class StringifyTest {

  private static final byte[] BYTES = {
    (byte) 0x0f,
    (byte) 0x5a,
    (byte) 0xbc,
    (byte) 0xd1,
    (byte) 0xc1,
    (byte) 0x94,
    (byte) 0x47,
    (byte) 0xf3,
    (byte) 0x90,
    (byte) 0x5b,
    (byte) 0x2d,
    (byte) 0xf7,
    (byte) 0x26,
    (byte) 0x3a,
    (byte) 0x08,
    (byte) 0x4b,
  };

  @Test
  @DisplayName("Stringify Array (unsafe)")
  void stringifyArrayUnsafe() {
    assertEquals("0f5abcd1-c194-47f3-905b-2df7263a084b", Stringify.unsafeStringify(BYTES));
  }

  @Test
  @DisplayName("Stringify w/ offset (unsafe)")
  void stringifyWithOffsetUnsafe() {
    byte[] bytes = new byte[19];
    Arrays.fill(bytes, (byte) 0);
    System.arraycopy(BYTES, 0, bytes, 3, BYTES.length);
    assertEquals("0f5abcd1-c194-47f3-905b-2df7263a084b", Stringify.unsafeStringify(bytes, 3));
  }

  @Test
  @DisplayName("Stringify Array (safe)")
  void stringifyArraySafe() {
    assertEquals("0f5abcd1-c194-47f3-905b-2df7263a084b", Stringify.stringify(BYTES));
  }

  @Test
  @DisplayName("Throws on not enough values (safe)")
  void throwsOnNotEnoughValues() {
    byte[] bytes = Arrays.copyOfRange(BYTES, 0, 15);
    assertThrows(JSTypeError.class, () -> Stringify.stringify(bytes));
  }
}
