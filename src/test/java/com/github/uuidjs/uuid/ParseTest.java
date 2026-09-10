package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/parse.test.ts}. */
@DisplayName("parse")
class ParseTest {

  /**
   * Deterministic PRNG for reproducible tests.
   *
   * <p>See https://stackoverflow.com/a/47593316/109538
   *
   * <p>Port note: JavaScript's {@code Math.imul} is exactly Java's {@code int}
   * multiplication (32-bit, wrapping, signed), and {@code x >>> 0} — which coerces to
   * an unsigned 32-bit value — becomes {@code x & 0xFFFFFFFFL}.
   */
  private static DoubleSupplier splitmix32(int seed) {
    int[] a = {seed};
    return () -> {
      a[0] = a[0] | 0;
      a[0] = a[0] + 0x9e3779b9;
      int t = a[0] ^ (a[0] >>> 16);
      t = t * 0x21f0aaad;
      t = t ^ (t >>> 15);
      t = t * 0x735a2d97;
      t = t ^ (t >>> 15);
      return (t & 0xFFFFFFFFL) / 4294967296.0;
    };
  }

  private final DoubleSupplier rand = splitmix32(0x12345678);

  /**
   * Mirrors the TS helper. Assignment into a {@code Uint8Array} applies
   * {@code ToUint8}, i.e. truncation toward zero; {@code rand()} is in {@code [0, 1)}
   * so {@code rand() * 256} truncates into {@code 0..255}.
   */
  private byte[] rng() {
    byte[] bytes = new byte[16];
    for (int i = 0; i < 16; i++) {
      bytes[i] = (byte) (int) (rand.getAsDouble() * 256);
    }
    return bytes;
  }

  @Test
  @DisplayName("String -> bytes parsing")
  void stringToBytesParsing() {
    assertArrayEquals(
        new byte[] {
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
        },
        Parse.parse("0f5abcd1-c194-47f3-905b-2df7263a084b"));
  }

  @Test
  @DisplayName("String -> bytes -> string symmetry for assorted uuids")
  void stringBytesStringSymmetry() {
    for (int i = 0; i < 1000; i++) {
      String uuid = V4.v4(new Version4Options().rng(this::rng));
      assertEquals(uuid, Stringify.stringify(Parse.parse(uuid)));
    }
  }

  @Test
  @DisplayName("Case neutrality")
  void caseNeutrality() {
    // Verify upper/lower case neutrality
    assertArrayEquals(
        Parse.parse("0f5abcd1-c194-47f3-905b-2df7263a084b"),
        Parse.parse("0f5abcd1-c194-47f3-905b-2df7263a084b".toUpperCase()));
  }

  @Test
  @DisplayName("Null UUID case")
  void nullUuidCase() {
    assertArrayEquals(new byte[16], Parse.parse("00000000-0000-0000-0000-000000000000"));
  }

  @Test
  @DisplayName("UUID validation")
  void uuidValidation() {
    // testing invalid input
    assertThrows(JSTypeError.class, () -> Parse.parse(null));

    assertThrows(JSTypeError.class, () -> Parse.parse("invalid uuid"));
    assertThrows(JSTypeError.class, () -> Parse.parse("zyxwvuts-rqpo-nmlk-jihg-fedcba000000"));
  }
}
