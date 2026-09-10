package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Edge cases that the upstream unit tests do not cover, but where JavaScript and
 * Java semantics differ enough that a naive port would silently diverge.
 *
 * <p><b>Every expected value in this file was produced by executing the original
 * TypeScript implementation</b> (via {@code dist-node}) and recording its output —
 * they are not derived from the Java port.
 */
@DisplayName("edge cases (verified against the TypeScript original)")
class EdgeCaseTest {

  private static final String ID = "0f5abcd1-c194-47f3-905b-2df7263a084b";
  private static final String DNS = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";

  @Test
  @DisplayName("unsafeStringify past the end of the array renders 'undefined'")
  void unsafeStringifyPastEnd() {
    // TS: byteToHex[arr[15]] where arr[15] is undefined -> the string "undefined".
    byte[] short15 = java.util.Arrays.copyOfRange(Parse.parse(ID), 0, 15);
    assertEquals(
        "0f5abcd1-c194-47f3-905b-2df7263a08undefined", Stringify.unsafeStringify(short15));
  }

  @Test
  @DisplayName("stringify with a negative offset throws TypeError (not RangeError)")
  void stringifyNegativeOffset() {
    // The negative index reads `undefined`, producing an invalid UUID string, so the
    // failure surfaces from validate() as a TypeError.
    assertThrows(JSTypeError.class, () -> Stringify.stringify(Parse.parse(ID), -1));
  }

  @Test
  @DisplayName("v3/v5 accept a byte[] value")
  void v3v5AcceptByteArrayValue() {
    byte[] hello = {104, 101, 108, 108, 111};
    assertEquals("0bacede4-4014-3f9d-b720-173f68a1c933", V3.v3(hello, DNS));
    assertEquals("9342d47a-1bab-5709-9869-c840b2eac501", V5.v5(hello, DNS));
  }

  @Test
  @DisplayName("v1 accepts nsecs beyond the 10000 cap when passed explicitly")
  void v1NsecsBeyondCap() {
    byte[] node = {1, 2, 3, 4, 5, 6};
    assertEquals(
        "8beec1d0-121c-11e1-b3c8-010203040506",
        V1.v1(
            new Version1Options()
                .msecs(1321644961388L)
                .nsecs(10000)
                .clockseq(0x33c8)
                .node(node.clone())));
    assertEquals(
        "8bf0215f-121c-11e1-b3c8-010203040506",
        V1.v1(
            new Version1Options()
                .msecs(1321644961388L)
                .nsecs(99999)
                .clockseq(0x33c8)
                .node(node.clone())));
  }

  @Test
  @DisplayName("v1 accepts a negative msecs")
  void v1NegativeMsecs() {
    assertEquals(
        "12e8a980-1dd2-11b2-8001-010203040506",
        V1.v1(
            new Version1Options()
                .msecs(-1000L)
                .nsecs(0)
                .clockseq(1)
                .node(new byte[] {1, 2, 3, 4, 5, 6})));
  }

  @Test
  @DisplayName("v1 zero-fills a node shorter than 6 bytes")
  void v1ShortNode() {
    // Reading past a Uint8Array yields undefined, which stores as 0.
    assertEquals(
        "13816710-1dd2-11b2-8001-010200000000",
        V1.v1(new Version1Options().msecs(1L).nsecs(0).clockseq(1).node(new byte[] {1, 2})));
  }

  @Test
  @DisplayName("v7 truncates msecs to 48 bits")
  void v7TruncatesMsecs() {
    assertEquals(
        "ffffffff-ffff-7000-8000-000000000000",
        V7.v7(new Version7Options().msecs(281474976710655L).seq(0).random(new byte[16])));
    assertEquals(
        "00000000-0000-7000-8000-000000000000",
        V7.v7(new Version7Options().msecs(281474976710656L).seq(0).random(new byte[16])));
  }

  @Test
  @DisplayName("v7 handles a negative (32-bit wrapped) seq")
  void v7NegativeSeq() {
    assertEquals(
        "00000000-0001-7fff-bfff-fc0000000000",
        V7.v7(new Version7Options().msecs(1L).seq(-1).random(new byte[16])));
  }

  @Test
  @DisplayName("rng() returns the same shared buffer on every call")
  void rngReturnsSharedBuffer() {
    // TS: `const rnds8 = new Uint8Array(16)` is module-level and reused.
    assertSame(Rng.rng(), Rng.rng());
  }

  @Test
  @DisplayName("v35 rejects a namespace that is not exactly 16 bytes")
  void v35RejectsWrongSizedNamespace() {
    assertThrows(JSTypeError.class, () -> V3.v3("x", new byte[17]));
    assertThrows(JSTypeError.class, () -> V3.v3("x", new byte[15]));
    assertThrows(JSTypeError.class, () -> V5.v5("x", new byte[17]));
  }

  @Test
  @DisplayName("stringToBytes throws URIError on an unpaired surrogate")
  void stringToBytesUnpairedSurrogate() {
    // TS: encodeURIComponent('\uD800') throws `URIError: URI malformed`.
    // Java's getBytes(UTF_8) would instead substitute '?' and silently produce a
    // different UUID, so the pairing is validated explicitly.
    assertThrows(JSURIError.class, () -> V35.stringToBytes("\uD800"));
    assertThrows(JSURIError.class, () -> V35.stringToBytes("\uDC00"));
    assertThrows(JSURIError.class, () -> V35.stringToBytes("a\uD800b"));
    assertThrows(JSURIError.class, () -> V5.v5("\uD800", DNS));

    // Well-formed surrogate pairs must still work.
    assertEquals(4, V35.stringToBytes("\uD83C\uDF89").length); // U+1F389 -> 4 UTF-8 bytes
  }

  @Test
  @DisplayName("v3/v5 throw TypeError for a null value when the namespace is valid")
  void v35NullValueWithValidNamespace() {
    // TS: v3(undefined, DNS) -> "TypeError: Cannot read properties of undefined
    // (reading 'length')". The namespace check passes first, then `valueBytes.length`
    // dereferences undefined.
    assertThrows(JSTypeError.class, () -> V3.v3((byte[]) null, DNS));
    assertThrows(JSTypeError.class, () -> V5.v5((byte[]) null, DNS));
    assertThrows(JSTypeError.class, () -> V3.v3((byte[]) null, Parse.parse(DNS)));
    assertThrows(JSTypeError.class, () -> V5.v5((byte[]) null, Parse.parse(DNS)));
  }

  @Test
  @DisplayName("v6 with empty options produces a valid v6 UUID")
  void v6EmptyOptions() {
    String id = V6.v6(new Version1Options());
    assertEquals(36, id.length());
    assertEquals('6', id.charAt(14));
  }
}
