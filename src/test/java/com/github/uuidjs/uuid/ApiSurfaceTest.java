package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers Java-specific API surface introduced by the migration: the extra typed
 * overloads, the options value-objects, and the state value-objects.
 *
 * <p>These have no direct TypeScript counterpart (TS uses a single variadic function
 * plus structural object literals), so they need dedicated tests rather than
 * inheriting coverage from the ported suite.
 */
@DisplayName("Java API surface")
class ApiSurfaceTest {

  private static final String DNS = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";

  // -------------------------------------------------------------------------
  // v3 / v5 overload matrix
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("v3 overloads all agree for equivalent inputs")
  void v3OverloadsAgree() {
    String expected = "9125a8dc-52ee-365b-a5aa-81b0b3681cf6";
    byte[] nsBytes = Parse.parse(DNS);
    byte[] valueBytes = V35.stringToBytes("hello.example.com");

    assertEquals(expected, V3.v3("hello.example.com", DNS));
    assertEquals(expected, V3.v3("hello.example.com", nsBytes));
    assertEquals(expected, V3.v3(valueBytes, DNS));
    assertEquals(expected, V3.v3(valueBytes, nsBytes));

    byte[] a = new byte[16];
    byte[] b = new byte[16];
    byte[] c = new byte[16];
    assertSame(a, V3.v3("hello.example.com", nsBytes, a));
    assertSame(b, V3.v3(valueBytes, DNS, b));
    assertSame(c, V3.v3("hello.example.com", DNS, c, 0));
    assertArrayEquals(Parse.parse(expected), a);
    assertArrayEquals(a, b);
    assertArrayEquals(a, c);

    byte[] off = new byte[20];
    V3.v3(valueBytes, DNS, off, 4);
    assertArrayEquals(Parse.parse(expected), Arrays.copyOfRange(off, 4, 20));
    byte[] off2 = new byte[20];
    V3.v3("hello.example.com", nsBytes, off2, 4);
    assertArrayEquals(Parse.parse(expected), Arrays.copyOfRange(off2, 4, 20));
  }

  @Test
  @DisplayName("v5 overloads all agree for equivalent inputs")
  void v5OverloadsAgree() {
    String expected = "fdda765f-fc57-5604-a269-52a7df8164ec";
    byte[] nsBytes = Parse.parse(DNS);
    byte[] valueBytes = V35.stringToBytes("hello.example.com");

    assertEquals(expected, V5.v5("hello.example.com", DNS));
    assertEquals(expected, V5.v5("hello.example.com", nsBytes));
    assertEquals(expected, V5.v5(valueBytes, DNS));
    assertEquals(expected, V5.v5(valueBytes, nsBytes));

    byte[] a = new byte[16];
    byte[] b = new byte[16];
    byte[] c = new byte[16];
    assertSame(a, V5.v5("hello.example.com", nsBytes, a));
    assertSame(b, V5.v5(valueBytes, DNS, b));
    assertSame(c, V5.v5("hello.example.com", DNS, c, 0));
    assertArrayEquals(Parse.parse(expected), a);
    assertArrayEquals(a, b);
    assertArrayEquals(a, c);

    byte[] off = new byte[20];
    V5.v5(valueBytes, DNS, off, 4);
    assertArrayEquals(Parse.parse(expected), Arrays.copyOfRange(off, 4, 20));
    byte[] off2 = new byte[20];
    V5.v5("hello.example.com", nsBytes, off2, 4);
    assertArrayEquals(Parse.parse(expected), Arrays.copyOfRange(off2, 4, 20));
  }

  // -------------------------------------------------------------------------
  // Options objects
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Version1Options.copy() preserves every key (Object.keys spread)")
  void version1OptionsCopy() {
    byte[] node = {1, 2, 3, 4, 5, 6};
    byte[] random = new byte[16];
    Arrays.fill(random, (byte) 7);

    Version1Options original =
        new Version1Options()
            .node(node)
            .clockseq(0x1234)
            .random(random)
            .rng(() -> random)
            .msecs(42L)
            .nsecs(7)
            .v6(true);

    Version1Options copy = original.copy();

    assertEquals(original.keys(), copy.keys());
    assertEquals(7, copy.keys().size());
    assertArrayEquals(node, copy.node());
    assertEquals(Integer.valueOf(0x1234), copy.clockseq());
    assertArrayEquals(random, copy.random());
    assertNotNull(copy.rng());
    assertEquals(Long.valueOf(42L), copy.msecs());
    assertEquals(Integer.valueOf(7), copy.nsecs());
    assertTrue(copy.isV6());

    // Copy is independent: mutating the copy's key set must not affect the original.
    copy.msecs(99L);
    assertEquals(Long.valueOf(42L), original.msecs());
  }

  @Test
  @DisplayName("Version1Options tracks key presence like Object.keys()")
  void version1OptionsKeyTracking() {
    assertEquals(0, new Version1Options().keys().size());
    assertEquals(1, new Version1Options().v6(true).keys().size());
    assertEquals(2, new Version1Options().msecs(1L).v6(true).keys().size());

    // Setting the same key twice still yields one key (JS object semantics).
    assertEquals(1, new Version1Options().msecs(1L).msecs(2L).keys().size());

    // A null-valued key is still a *present* key — this is the distinction that a
    // naive "all fields null" check would get wrong.
    Version1Options nulled = new Version1Options().msecs((Long) null).v6(true);
    assertEquals(2, nulled.keys().size());
  }

  @Test
  @DisplayName("v6 with only _v6 present routes through internal state (not the options path)")
  void v6OnlyFlagRoutesToInternalState() {
    // v6({}) spreads to {_v6: true}; v1() then collapses that back to `undefined`.
    // Observable consequence: clockseq/node are re-randomised per call, so two
    // successive calls differ in the node field.
    String a = V6.v6(new Version1Options());
    String b = V6.v6(new Version1Options());
    assertNotEquals(a, b);
    assertEquals(6, Version.version(a));
    assertEquals(6, Version.version(b));
  }

  @Test
  @DisplayName("Version6Options is interchangeable with Version1Options")
  void version6OptionsAlias() {
    Version6Options options = new Version6Options();
    options.msecs(0x133b891f705L).nsecs(0x1538).clockseq(0x385c)
        .node(new byte[] {(byte) 0x61, (byte) 0xcd, (byte) 0x3c, (byte) 0xbb, 0x32, 0x10});
    assertEquals("1e1122bd-9428-6888-b85c-61cd3cbb3210", V6.v6(options));
  }

  @Test
  @DisplayName("Version4Options / Version7Options accessors round-trip")
  void optionAccessors() {
    byte[] r = new byte[16];
    Version4Options v4 = new Version4Options().random(r).rng(() -> r);
    assertSame(r, v4.random());
    assertSame(r, v4.rng().get());

    Version7Options v7 = new Version7Options().random(r).rng(() -> r).msecs(5L).seq(9);
    assertSame(r, v7.random());
    assertSame(r, v7.rng().get());
    assertEquals(Long.valueOf(5L), v7.msecs());
    assertEquals(Integer.valueOf(9), v7.seq());
  }

  // -------------------------------------------------------------------------
  // rng fallback: a supplier returning null must fall through to Rng.rng()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("a rng supplier returning null falls back to the default rng")
  void nullRngFallsBack() {
    // Mirrors `options.random ?? options.rng?.() ?? rng()` where rng() yields
    // null/undefined.
    String id1 = V1.v1(new Version1Options().msecs(1L).rng(() -> null));
    assertTrue(Validate.validate(id1));
    assertEquals(1, Version.version(id1));

    String id4 = V4.v4(new Version4Options().rng(() -> null));
    assertTrue(Validate.validate(id4));
    assertEquals(4, Version.version(id4));

    String id7 = V7.v7(new Version7Options().msecs(1L).rng(() -> null));
    assertTrue(Validate.validate(id7));
    assertEquals(7, Version.version(id7));
  }

  @Test
  @DisplayName("v4(null, buf) uses the default rng and fills the buffer")
  void v4NullOptionsWithBuffer() {
    byte[] buf = new byte[16];
    assertSame(buf, V4.v4(null, buf));
    assertTrue(Validate.validate(Stringify.unsafeStringify(buf)));
    assertEquals(4, Version.version(Stringify.unsafeStringify(buf)));
  }

  @Test
  @DisplayName("v6(options, null, offset) returns freshly allocated bytes")
  void v6NullBufferReturnsBytes() {
    byte[] bytes = V6.v6(new Version1Options().msecs(0x133b891f705L), null, 0);
    assertEquals(16, bytes.length);
    assertEquals(6, Version.version(Stringify.unsafeStringify(bytes)));
  }

  // -------------------------------------------------------------------------
  // State value objects (equals/hashCode/toString exist for deepStrictEqual parity)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("V1State value semantics")
  void v1StateValueSemantics() {
    byte[] node = {1, 2, 3, 4, 5, 6};
    V1State a = new V1State(1L, 2, 3, node.clone());
    V1State b = new V1State(1L, 2, 3, node.clone());
    V1State different = new V1State(1L, 2, 3, new byte[] {9, 9, 9, 9, 9, 9});

    assertEquals(a, a);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, different);
    assertNotEquals(a, new V1State());
    assertNotEquals(a, "not a state");
    assertNotEquals(a, null);

    assertEquals(Long.valueOf(1L), a.msecs());
    assertEquals(Integer.valueOf(2), a.nsecs());
    assertEquals(Integer.valueOf(3), a.clockseq());
    assertArrayEquals(node, a.node());

    assertTrue(a.toString().contains("msecs=1"));
    assertTrue(a.toString().contains("nsecs=2"));
    assertTrue(new V1State().toString().contains("node=null"));
  }

  @Test
  @DisplayName("V7State value semantics")
  void v7StateValueSemantics() {
    V7State a = new V7State(1L, 2);
    V7State b = new V7State(1L, 2);

    assertEquals(a, a);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, new V7State(1L, 3));
    assertNotEquals(a, new V7State(2L, 2));
    assertNotEquals(a, "not a state");
    assertNotEquals(a, null);

    assertEquals(Long.valueOf(1L), a.msecs());
    assertEquals(Integer.valueOf(2), a.seq());
    assertTrue(a.toString().contains("msecs=1"));
    assertTrue(a.toString().contains("seq=2"));
  }

  @Test
  @DisplayName("updateV1State/updateV7State initialise a null seq/nsecs")
  void stateNullInitialisation() {
    byte[] rnds = new byte[16];
    Arrays.fill(rnds, (byte) 0x11);

    // nsecs starts null -> initialised to 0
    V1State s1 = V1.updateV1State(new V1State(null, null, null, null), 5L, rnds);
    assertEquals(Long.valueOf(5L), s1.msecs());
    assertEquals(Integer.valueOf(0), s1.nsecs());
    assertNotNull(s1.node());

    // seq starts null -> initialised to 0, then randomised because now > -Infinity
    V7State s7 = V7.updateV7State(new V7State(null, null), 5L, rnds);
    assertEquals(Long.valueOf(5L), s7.msecs());
    assertNotNull(s7.seq());
  }
}
