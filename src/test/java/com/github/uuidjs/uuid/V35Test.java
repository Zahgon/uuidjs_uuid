package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/v35.test.ts}. */
@DisplayName("v35")
class V35Test {

  private record HashSample(byte[] input, String sha1, String md5) {}

  private static final List<HashSample> HASH_SAMPLES =
      List.of(
          new HashSample(
              V35.stringToBytes(""),
              "da39a3ee5e6b4b0d3255bfef95601890afd80709",
              "d41d8cd98f00b204e9800998ecf8427e"),

          // Extended ascii chars
          new HashSample(
              V35.stringToBytes(
                  "\t\b\f  !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`"
                      + "abcdefghijklmnopqrstuvwxyz{|}~\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6"
                      + "\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AE\u00AF\u00B0\u00B1\u00B2"
                      + "\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD"
                      + "\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8"
                      + "\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3"
                      + "\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE"
                      + "\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9"
                      + "\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4"
                      + "\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF"),
              "ca4a426a3d536f14cfd79011e79e10d64de950a0",
              "e8098ec21950f841731d28749129d3ee"),

          // A sampling from the Unicode BMP
          new HashSample(
              V35.stringToBytes(
                  "\u00A5\u0104\u018F\u0256\u02B1o\u0315\u038E\u0409\u0500\u0531\u05E1\u05B6"
                      + "\u0920\u0903\u09A4\u0983\u0A20\u0A02\u0AA0\u0A83\u0B06\u0C05\u0C03"
                      + "\u1401\u16A0"),
              "f2753ebc390e5f637e333c2a4179644a93ae9f65",
              "231b309e277b6be8bb3d6c688b7f098b"));

  private static String hashToHex(byte[] hash) {
    StringBuilder sb = new StringBuilder(hash.length * 2);
    for (byte b : hash) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

  @Test
  @DisplayName("sha1(node) HASH_SAMPLES[0]")
  void sha1Sample0() {
    assertEquals(HASH_SAMPLES.get(0).sha1(), hashToHex(Sha1.sha1(HASH_SAMPLES.get(0).input())));
  }

  @Test
  @DisplayName("sha1(node) HASH_SAMPLES[1]")
  void sha1Sample1() {
    assertEquals(HASH_SAMPLES.get(1).sha1(), hashToHex(Sha1.sha1(HASH_SAMPLES.get(1).input())));
  }

  @Test
  @DisplayName("sha1(node) HASH_SAMPLES[2]")
  void sha1Sample2() {
    assertEquals(HASH_SAMPLES.get(2).sha1(), hashToHex(Sha1.sha1(HASH_SAMPLES.get(2).input())));
  }

  @Test
  @DisplayName("md5(node) HASH_SAMPLES[0]")
  void md5Sample0() {
    assertEquals(HASH_SAMPLES.get(0).md5(), hashToHex(Md5.md5(HASH_SAMPLES.get(0).input())));
  }

  @Test
  @DisplayName("md5(node) HASH_SAMPLES[1]")
  void md5Sample1() {
    assertEquals(HASH_SAMPLES.get(1).md5(), hashToHex(Md5.md5(HASH_SAMPLES.get(1).input())));
  }

  @Test
  @DisplayName("md5(node) HASH_SAMPLES[2]")
  void md5Sample2() {
    assertEquals(HASH_SAMPLES.get(2).md5(), hashToHex(Md5.md5(HASH_SAMPLES.get(2).input())));
  }

  @Test
  @DisplayName("v3")
  void v3() {
    // Expect to get the same results as http://tools.adjet.org/uuid-v3
    assertEquals("9125a8dc-52ee-365b-a5aa-81b0b3681cf6", V3.v3("hello.example.com", V3.DNS));

    assertEquals("c6235813-3ba4-3801-ae84-e0a6ebb7d138", V3.v3("http://example.com/hello", V3.URL));

    assertEquals(
        "a981a0c2-68b1-35dc-bcfc-296e52ab01ec",
        V3.v3("hello", "0f5abcd1-c194-47f3-905b-2df7263a084b"));
  }

  @Test
  @DisplayName("v3 namespace.toUpperCase")
  void v3NamespaceToUpperCase() {
    assertEquals(
        "9125a8dc-52ee-365b-a5aa-81b0b3681cf6",
        V3.v3("hello.example.com", V3.DNS.toUpperCase()));

    assertEquals(
        "c6235813-3ba4-3801-ae84-e0a6ebb7d138",
        V3.v3("http://example.com/hello", V3.URL.toUpperCase()));

    assertEquals(
        "a981a0c2-68b1-35dc-bcfc-296e52ab01ec",
        V3.v3("hello", "0f5abcd1-c194-47f3-905b-2df7263a084b".toUpperCase()));
  }

  @Test
  @DisplayName("v3 namespace string validation")
  void v3NamespaceStringValidation() {
    assertThrows(
        JSTypeError.class,
        () -> V3.v3("hello.example.com", "zyxwvuts-rqpo-nmlk-jihg-fedcba000000"));

    assertThrows(JSTypeError.class, () -> V3.v3("hello.example.com", "invalid uuid value"));

    assertNotNull(V3.v3("hello.example.com", "00000000-0000-0000-0000-000000000000"));
  }

  @Test
  @DisplayName("v3 namespace buffer validation")
  void v3NamespaceBufferValidation() {
    assertThrows(JSTypeError.class, () -> V3.v3("hello.example.com", new byte[15]));

    assertThrows(JSTypeError.class, () -> V3.v3("hello.example.com", new byte[17]));

    assertNotNull(V3.v3("hello.example.com", new byte[16]));
  }

  @Test
  @DisplayName("v3 fill buffer")
  void v3FillBuffer() {
    byte[] buf = new byte[16];

    byte[] expectedUuid = {
      (byte) 0x91,
      (byte) 0x25,
      (byte) 0xa8,
      (byte) 0xdc,
      (byte) 0x52,
      (byte) 0xee,
      (byte) 0x36,
      (byte) 0x5b,
      (byte) 0xa5,
      (byte) 0xaa,
      (byte) 0x81,
      (byte) 0xb0,
      (byte) 0xb3,
      (byte) 0x68,
      (byte) 0x1c,
      (byte) 0xf6,
    };

    byte[] result = V3.v3("hello.example.com", V3.DNS, buf);

    assertArrayEquals(expectedUuid, buf);
    assertSame(buf, result);

    // test offsets as well
    buf = new byte[19];
    Arrays.fill(buf, (byte) 0xaa);

    byte[] expectedBuf = new byte[19];
    Arrays.fill(expectedBuf, (byte) 0xaa);
    System.arraycopy(expectedUuid, 0, expectedBuf, 3, 16);

    V3.v3("hello.example.com", V3.DNS, buf, 3);

    assertArrayEquals(expectedBuf, buf);
  }

  @Test
  @DisplayName("v3 undefined/null")
  void v3UndefinedNull() {
    // testing invalid input
    assertThrows(JSTypeError.class, () -> V3.v3((String) null, (String) null));
    assertThrows(JSTypeError.class, () -> V3.v3("hello", (String) null));
    assertThrows(JSTypeError.class, () -> V3.v3("hello.example.com", (String) null));
    assertThrows(
        JSTypeError.class, () -> V3.v3("hello.example.com", (String) null, new byte[16]));
  }

  @Test
  @DisplayName("v3 throws RangeError for out-of-range indexes")
  void v3ThrowsRangeError() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V3.v3("hello.example.com", V3.DNS, buf15));
    assertThrows(JSRangeError.class, () -> V3.v3("hello.example.com", V3.DNS, buf30, -1));
    assertThrows(JSRangeError.class, () -> V3.v3("hello.example.com", V3.DNS, buf30, 15));
  }

  @Test
  @DisplayName("v5")
  void v5() {
    // Expect to get the same results as http://tools.adjet.org/uuid-v5
    assertEquals("fdda765f-fc57-5604-a269-52a7df8164ec", V5.v5("hello.example.com", V5.DNS));

    assertEquals("3bbcee75-cecc-5b56-8031-b6641c1ed1f1", V5.v5("http://example.com/hello", V5.URL));

    assertEquals(
        "90123e1c-7512-523e-bb28-76fab9f2f73d",
        V5.v5("hello", "0f5abcd1-c194-47f3-905b-2df7263a084b"));
  }

  @Test
  @DisplayName("v5 namespace.toUpperCase")
  void v5NamespaceToUpperCase() {
    // Expect to get the same results as http://tools.adjet.org/uuid-v5
    assertEquals(
        "fdda765f-fc57-5604-a269-52a7df8164ec",
        V5.v5("hello.example.com", V5.DNS.toUpperCase()));

    assertEquals(
        "3bbcee75-cecc-5b56-8031-b6641c1ed1f1",
        V5.v5("http://example.com/hello", V5.URL.toUpperCase()));

    assertEquals(
        "90123e1c-7512-523e-bb28-76fab9f2f73d",
        V5.v5("hello", "0f5abcd1-c194-47f3-905b-2df7263a084b".toUpperCase()));
  }

  @Test
  @DisplayName("v5 namespace string validation")
  void v5NamespaceStringValidation() {
    assertThrows(
        JSTypeError.class,
        () -> V5.v5("hello.example.com", "zyxwvuts-rqpo-nmlk-jihg-fedcba000000"));

    assertThrows(JSTypeError.class, () -> V5.v5("hello.example.com", "invalid uuid value"));

    assertNotNull(V5.v5("hello.example.com", "00000000-0000-0000-0000-000000000000"));
  }

  @Test
  @DisplayName("v5 namespace buffer validation")
  void v5NamespaceBufferValidation() {
    assertThrows(JSTypeError.class, () -> V5.v5("hello.example.com", new byte[15]));

    assertThrows(JSTypeError.class, () -> V5.v5("hello.example.com", new byte[17]));

    assertNotNull(V5.v5("hello.example.com", new byte[16]));
  }

  @Test
  @DisplayName("v5 fill buffer")
  void v5FillBuffer() {
    byte[] buf = new byte[16];

    byte[] expectedUuid = {
      (byte) 0xfd,
      (byte) 0xda,
      (byte) 0x76,
      (byte) 0x5f,
      (byte) 0xfc,
      (byte) 0x57,
      (byte) 0x56,
      (byte) 0x04,
      (byte) 0xa2,
      (byte) 0x69,
      (byte) 0x52,
      (byte) 0xa7,
      (byte) 0xdf,
      (byte) 0x81,
      (byte) 0x64,
      (byte) 0xec,
    };

    byte[] result = V5.v5("hello.example.com", V5.DNS, buf);
    assertArrayEquals(expectedUuid, buf);
    assertSame(buf, result);

    // test offsets as well
    buf = new byte[19];
    Arrays.fill(buf, (byte) 0xaa);

    byte[] expectedBuf = new byte[19];
    Arrays.fill(expectedBuf, (byte) 0xaa);
    System.arraycopy(expectedUuid, 0, expectedBuf, 3, 16);

    V5.v5("hello.example.com", V5.DNS, buf, 3);

    assertArrayEquals(expectedBuf, buf);
  }

  @Test
  @DisplayName("v5 undefined/null")
  void v5UndefinedNull() {
    // testing invalid input
    assertThrows(JSTypeError.class, () -> V5.v5((String) null, (String) null));
    assertThrows(JSTypeError.class, () -> V5.v5("hello", (String) null));
    assertThrows(JSTypeError.class, () -> V5.v5("hello.example.com", (String) null));
    assertThrows(
        JSTypeError.class, () -> V5.v5("hello.example.com", (String) null, new byte[16]));
  }

  @Test
  @DisplayName("v5 throws RangeError for out-of-range indexes")
  void v5ThrowsRangeError() {
    byte[] buf15 = new byte[15];
    byte[] buf30 = new byte[30];
    assertThrows(JSRangeError.class, () -> V5.v5("hello.example.com", V5.DNS, buf15));
    assertThrows(JSRangeError.class, () -> V5.v5("hello.example.com", V5.DNS, buf30, -1));
    assertThrows(JSRangeError.class, () -> V5.v5("hello.example.com", V5.DNS, buf30, 15));
  }

  @Test
  @DisplayName("v3/v5 constants")
  void v3v5Constants() {
    assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", V3.DNS);
    assertEquals("6ba7b811-9dad-11d1-80b4-00c04fd430c8", V3.URL);
    assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", V5.DNS);
    assertEquals("6ba7b811-9dad-11d1-80b4-00c04fd430c8", V5.URL);
  }
}
