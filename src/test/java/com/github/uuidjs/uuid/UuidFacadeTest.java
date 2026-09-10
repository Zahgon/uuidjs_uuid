package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the {@link Uuid} facade — the port of {@code src/index.ts} — re-exports
 * every symbol and delegates without altering behaviour.
 *
 * <p>{@code index.ts} is pure re-exports, so the TypeScript test suite never loads
 * it (its tests import the individual modules directly). The Java facade is real
 * code, so it needs real tests.
 */
@DisplayName("Uuid facade (index.ts)")
class UuidFacadeTest {

  private static final String ID = "0f5abcd1-c194-47f3-905b-2df7263a084b";
  private static final byte[] RFC_NODE = {
    (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
  };

  private static Version1Options rfcOptions() {
    return new Version1Options()
        .msecs(0x17f22e279b0L)
        .nsecs(0)
        .clockseq(0x33c8)
        .node(RFC_NODE.clone());
  }

  @Test
  @DisplayName("MAX and NIL constants")
  void constants() {
    assertEquals("ffffffff-ffff-ffff-ffff-ffffffffffff", Uuid.MAX);
    assertEquals("00000000-0000-0000-0000-000000000000", Uuid.NIL);
    assertEquals(Max.MAX, Uuid.MAX);
    assertEquals(Nil.NIL, Uuid.NIL);
  }

  @Test
  @DisplayName("parse / stringify delegate correctly")
  void parseStringify() {
    assertArrayEquals(Parse.parse(ID), Uuid.parse(ID));
    assertEquals(ID, Uuid.stringify(Uuid.parse(ID)));

    byte[] padded = new byte[24];
    Arrays.fill(padded, (byte) 0xaa);
    System.arraycopy(Parse.parse(ID), 0, padded, 5, 16);
    assertEquals(ID, Uuid.stringify(padded, 5));

    assertThrows(JSTypeError.class, () -> Uuid.parse("not-a-uuid"));
  }

  @Test
  @DisplayName("validate / version delegate correctly")
  void validateVersion() {
    assertTrue(Uuid.validate(ID));
    assertEquals(false, Uuid.validate("nope"));
    assertEquals(false, Uuid.validate(null));
    assertEquals(4, Uuid.version(ID));
    assertEquals(0, Uuid.version(Uuid.NIL));
    assertEquals(15, Uuid.version(Uuid.MAX));
    assertThrows(JSTypeError.class, () -> Uuid.version("nope"));
  }

  @Test
  @DisplayName("v1 overloads delegate correctly")
  void v1Overloads() {
    String expected = "c232ab00-9414-11ec-b3c8-9f68deced846";
    assertEquals(expected, Uuid.v1(rfcOptions()));

    byte[] buf = new byte[16];
    assertSame(buf, Uuid.v1(rfcOptions(), buf));
    assertArrayEquals(Parse.parse(expected), buf);

    byte[] buf32 = new byte[32];
    Uuid.v1(rfcOptions(), buf32, 16);
    assertArrayEquals(Parse.parse(expected), Arrays.copyOfRange(buf32, 16, 32));

    String generated = Uuid.v1();
    assertTrue(Uuid.validate(generated));
    assertEquals(1, Uuid.version(generated));
  }

  @Test
  @DisplayName("v3 overloads delegate correctly")
  void v3Overloads() {
    assertEquals(
        "9125a8dc-52ee-365b-a5aa-81b0b3681cf6", Uuid.v3("hello.example.com", V3.DNS));

    byte[] buf = new byte[16];
    assertSame(buf, Uuid.v3("hello.example.com", V3.DNS, buf));
    assertEquals("9125a8dc-52ee-365b-a5aa-81b0b3681cf6", Stringify.unsafeStringify(buf));

    byte[] buf19 = new byte[19];
    Uuid.v3("hello.example.com", V3.DNS, buf19, 3);
    assertEquals("9125a8dc-52ee-365b-a5aa-81b0b3681cf6", Stringify.unsafeStringify(buf19, 3));
  }

  @Test
  @DisplayName("v4 overloads delegate correctly")
  void v4Overloads() {
    byte[] fixture = {
      (byte) 0x10, (byte) 0x91, (byte) 0x56, (byte) 0xbe,
      (byte) 0xc4, (byte) 0xfb, (byte) 0xc1, (byte) 0xea,
      (byte) 0x71, (byte) 0xb4, (byte) 0xef, (byte) 0xe1,
      (byte) 0x67, (byte) 0x1c, (byte) 0x58, (byte) 0x36
    };
    assertEquals(
        "109156be-c4fb-41ea-b1b4-efe1671c5836",
        Uuid.v4(new Version4Options().random(fixture.clone())));

    byte[] buf = new byte[16];
    assertSame(buf, Uuid.v4(new Version4Options().random(fixture.clone()), buf));

    byte[] buf32 = new byte[32];
    Uuid.v4(new Version4Options().random(fixture.clone()), buf32, 16);
    assertEquals(
        "109156be-c4fb-41ea-b1b4-efe1671c5836", Stringify.unsafeStringify(buf32, 16));

    String generated = Uuid.v4();
    assertTrue(Uuid.validate(generated));
    assertEquals(4, Uuid.version(generated));
  }

  @Test
  @DisplayName("v5 overloads delegate correctly")
  void v5Overloads() {
    assertEquals(
        "fdda765f-fc57-5604-a269-52a7df8164ec", Uuid.v5("hello.example.com", V5.DNS));

    byte[] buf = new byte[16];
    assertSame(buf, Uuid.v5("hello.example.com", V5.DNS, buf));
    assertEquals("fdda765f-fc57-5604-a269-52a7df8164ec", Stringify.unsafeStringify(buf));

    byte[] buf19 = new byte[19];
    Uuid.v5("hello.example.com", V5.DNS, buf19, 3);
    assertEquals("fdda765f-fc57-5604-a269-52a7df8164ec", Stringify.unsafeStringify(buf19, 3));
  }

  @Test
  @DisplayName("v6 overloads delegate correctly")
  void v6Overloads() {
    Version1Options full =
        new Version1Options()
            .msecs(0x133b891f705L)
            .nsecs(0x1538)
            .clockseq(0x385c)
            .node(new byte[] {(byte) 0x61, (byte) 0xcd, (byte) 0x3c, (byte) 0xbb, 0x32, 0x10});
    assertEquals("1e1122bd-9428-6888-b85c-61cd3cbb3210", Uuid.v6(full));

    byte[] buf = new byte[16];
    assertSame(buf, Uuid.v6(full, buf));

    byte[] buf32 = new byte[32];
    Uuid.v6(full, buf32, 16);
    assertEquals("1e1122bd-9428-6888-b85c-61cd3cbb3210", Stringify.unsafeStringify(buf32, 16));

    String generated = Uuid.v6();
    assertTrue(Uuid.validate(generated));
    assertEquals(6, Uuid.version(generated));
  }

  @Test
  @DisplayName("v7 overloads delegate correctly")
  void v7Overloads() {
    byte[] random = {
      (byte) 0x10, (byte) 0x91, (byte) 0x56, (byte) 0xbe,
      (byte) 0xc4, (byte) 0xfb, (byte) 0x0c, (byte) 0xc3,
      (byte) 0x18, (byte) 0xc4, (byte) 0x6c, (byte) 0x0c,
      (byte) 0x0c, (byte) 0x07, (byte) 0x39, (byte) 0x8f
    };
    int seq = (0x0cc3 << 20) | (0x98c4dc >> 2);
    assertEquals(
        "017f22e2-79b0-7cc3-98c4-dc0c0c07398f",
        Uuid.v7(new Version7Options().random(random.clone()).msecs(0x17f22e279b0L).seq(seq)));

    byte[] buf = new byte[16];
    assertSame(
        buf,
        Uuid.v7(
            new Version7Options().random(random.clone()).msecs(0x17f22e279b0L).seq(seq), buf));

    byte[] buf32 = new byte[32];
    Uuid.v7(
        new Version7Options().random(random.clone()).msecs(0x17f22e279b0L).seq(seq), buf32, 16);
    assertEquals(
        "017f22e2-79b0-7cc3-98c4-dc0c0c07398f", Stringify.unsafeStringify(buf32, 16));

    String generated = Uuid.v7();
    assertTrue(Uuid.validate(generated));
    assertEquals(7, Uuid.version(generated));
  }

  @Test
  @DisplayName("v1ToV6 / v6ToV1 delegate correctly (string and byte[])")
  void conversionDelegates() {
    String v1Id = "f1207660-21d2-11ef-8c4f-419efbd44d48";
    String v6Id = "1ef21d2f-1207-6660-8c4f-419efbd44d48";

    assertEquals(v6Id, Uuid.v1ToV6(v1Id));
    assertEquals(v1Id, Uuid.v6ToV1(v6Id));

    assertArrayEquals(Parse.parse(v6Id), Uuid.v1ToV6(Parse.parse(v1Id)));
    assertArrayEquals(Parse.parse(v1Id), Uuid.v6ToV1(Parse.parse(v6Id)));
  }
}
