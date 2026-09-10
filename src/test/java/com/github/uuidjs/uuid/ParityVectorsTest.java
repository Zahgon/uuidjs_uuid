package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Differential test against the ORIGINAL TypeScript implementation.
 *
 * <p>{@code harness/ts_vectors.mjs} runs the real {@code uuid} package and writes a
 * deterministic corpus to {@code src/test/resources/parity/ts_golden.txt.gz}. This
 * test regenerates the identical corpus using the Java port and asserts the two
 * agree line for line.
 *
 * <p>This is the assertion that actually establishes behavioural equivalence — the
 * ported unit tests alone only prove the port is internally consistent.
 */
@DisplayName("TypeScript parity")
class ParityVectorsTest {

  private final List<String> out = new ArrayList<>();

  private void emit(Object... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append('|');
      }
      sb.append(parts[i]);
    }
    out.add(sb.toString());
  }

  /** Deterministic PRNG (splitmix32) — identical to the JS harness. */
  private static DoubleSupplier splitmix32(int seed) {
    int[] a = {seed};
    return () -> {
      a[0] = a[0] + 0x9e3779b9;
      int t = a[0] ^ (a[0] >>> 16);
      t = t * 0x21f0aaad;
      t = t ^ (t >>> 15);
      t = t * 0x735a2d97;
      t = t ^ (t >>> 15);
      return (t & 0xFFFFFFFFL) / 4294967296.0;
    };
  }

  private static byte[] randomBytes(DoubleSupplier rand) {
    byte[] b = new byte[16];
    for (int j = 0; j < 16; j++) {
      b[j] = (byte) (int) (rand.getAsDouble() * 256);
    }
    return b;
  }

  private static String hex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

  /** Mirrors {@code JSON.stringify(str)} for the plain strings used in the corpus. */
  private static String jsonString(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }

  private void generate() {
    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------
    emit("const", "NIL", Nil.NIL);
    emit("const", "MAX", Max.MAX);
    emit("const", "v3.DNS", V3.DNS);
    emit("const", "v3.URL", V3.URL);
    emit("const", "v5.DNS", V5.DNS);
    emit("const", "v5.URL", V5.URL);

    // -----------------------------------------------------------------------
    // v3 / v5
    // -----------------------------------------------------------------------
    String[] names = {
      "",
      "a",
      "hello",
      "hello.example.com",
      "http://example.com/hello",
      "Ünïcödé-nämé",
      "日本語のテキスト",
      "🎉 emoji 🚀 surrogate pairs 🧪",
      "x".repeat(1000),
      "\t\n\r ",
      "mixed 123 !@#$%^&*()_+-=[]{}|;:\",.<>/?",
      "\u00A5\u0104\u018F\u0256\u02B1o\u0315\u038E\u0409\u0500",
    };
    String[] namespaces = {
      V3.DNS,
      V3.URL,
      Nil.NIL,
      Max.MAX,
      "0f5abcd1-c194-47f3-905b-2df7263a084b",
      "6BA7B810-9DAD-11D1-80B4-00C04FD430C8",
    };
    for (String name : names) {
      for (String ns : namespaces) {
        emit("v3", jsonString(name), ns, V3.v3(name, ns));
        emit("v5", jsonString(name), ns, V5.v5(name, ns));
      }
    }

    {
      byte[] buf = new byte[20];
      Arrays.fill(buf, (byte) 0xaa);
      V3.v3("hello.example.com", V3.DNS, buf, 4);
      emit("v3buf", hex(buf));
      byte[] buf2 = new byte[20];
      Arrays.fill(buf2, (byte) 0xaa);
      V5.v5("hello.example.com", V5.DNS, buf2, 4);
      emit("v5buf", hex(buf2));
    }

    // -----------------------------------------------------------------------
    // v1 / v6
    // -----------------------------------------------------------------------
    long[] v1Msecs = {
      0L, 1L, 1000L, 1321644961388L, 1321645585614L, 0x17f22e279b0L,
      1L << 40, (1L << 44) + 12345, 4398046511103L
    };
    int[] v1Nsecs = {0, 1, 4382, 4383, 4384, 4385, 9998, 9999};
    for (long msecs : v1Msecs) {
      for (int nsecs : v1Nsecs) {
        byte[] node = {
          (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
        };
        int clockseq = (int) ((msecs + nsecs) & 0x3fff);
        emit(
            "v1",
            msecs,
            nsecs,
            clockseq,
            hex(node),
            V1.v1(
                new Version1Options()
                    .msecs(msecs)
                    .nsecs(nsecs)
                    .clockseq(clockseq)
                    .node(node.clone())));
        emit(
            "v6",
            msecs,
            nsecs,
            clockseq,
            hex(node),
            V6.v6(
                new Version1Options()
                    .msecs(msecs)
                    .nsecs(nsecs)
                    .clockseq(clockseq)
                    .node(node.clone())));
      }
    }

    {
      DoubleSupplier rand = splitmix32(0xc0ffee);
      for (int i = 0; i < 200; i++) {
        byte[] random = randomBytes(rand);
        long msecs = 1321644961388L + (long) i * 7919;
        emit(
            "v1rand",
            msecs,
            hex(random),
            V1.v1(new Version1Options().msecs(msecs).random(random.clone())));
        emit(
            "v6rand",
            msecs,
            hex(random),
            V6.v6(new Version1Options().msecs(msecs).random(random.clone())));
      }
    }

    {
      byte[] buf = new byte[40];
      Arrays.fill(buf, (byte) 0x5a);
      V1.v1(
          new Version1Options()
              .msecs(0x17f22e279b0L)
              .nsecs(0)
              .clockseq(0x33c8)
              .node(
                  new byte[] {
                    (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
                  }),
          buf,
          7);
      emit("v1buf", hex(buf));
    }

    // -----------------------------------------------------------------------
    // v4
    // -----------------------------------------------------------------------
    {
      DoubleSupplier rand = splitmix32(0xbadc0de);
      for (int i = 0; i < 300; i++) {
        byte[] random = randomBytes(rand);
        String before = hex(random);
        String id = V4.v4(new Version4Options().random(random));
        // `random` is mutated in place by v4 — capture that side effect too.
        emit("v4", before, hex(random), id);
      }
    }

    // -----------------------------------------------------------------------
    // v7
    // -----------------------------------------------------------------------
    int[] v7Seqs = {
      0, 1, 0x12345, 0x6fffffff, 0x7fffffff, -1, -2147483648, 0xcc318c4, 0x0fffffff, 1 << 28
    };
    {
      DoubleSupplier rand = splitmix32(0x5eed);
      for (int seq : v7Seqs) {
        for (long msecs : new long[] {0L, 1L, 0x17f22e279b0L, 1L << 40, 281474976710655L}) {
          byte[] random = randomBytes(rand);
          emit(
              "v7",
              msecs,
              seq,
              hex(random),
              V7.v7(new Version7Options().msecs(msecs).seq(seq).random(random.clone())));
          emit(
              "v7noseq",
              msecs,
              hex(random),
              V7.v7(new Version7Options().msecs(msecs).random(random.clone())));
        }
      }
    }

    {
      byte[] buf = new byte[40];
      Arrays.fill(buf, (byte) 0x3c);
      byte[] r = new byte[16];
      Arrays.fill(r, (byte) 0x77);
      V7.v7(new Version7Options().msecs(0x17f22e279b0L).seq(0x12345).random(r), buf, 9);
      emit("v7buf", hex(buf));
    }

    // -----------------------------------------------------------------------
    // v1 <-> v6 conversions
    // -----------------------------------------------------------------------
    {
      DoubleSupplier rand = splitmix32(0x1234abcd);
      for (int i = 0; i < 200; i++) {
        byte[] random = randomBytes(rand);
        String id1 =
            V1.v1(
                new Version1Options()
                    .msecs(1000L + (long) i * 104729)
                    .nsecs(i % 10000)
                    .random(random));
        String id6 = V1ToV6.v1ToV6(id1);
        emit("v1tov6", id1, id6);
        emit("v6tov1", id6, V6ToV1.v6ToV1(id6));
        emit("v1tov6b", hex(V1ToV6.v1ToV6(Parse.parse(id1))));
        emit("v6tov1b", hex(V6ToV1.v6ToV1(Parse.parse(id6))));
      }
    }

    // -----------------------------------------------------------------------
    // parse / stringify
    // -----------------------------------------------------------------------
    {
      DoubleSupplier rand = splitmix32(0x99887766);
      for (int i = 0; i < 500; i++) {
        byte[] random = randomBytes(rand);
        String id = V4.v4(new Version4Options().random(random));
        emit("parse", id, hex(Parse.parse(id)));
        emit("stringify", hex(Parse.parse(id)), Stringify.stringify(Parse.parse(id)));
      }
      emit("parse", Nil.NIL, hex(Parse.parse(Nil.NIL)));
      emit("parse", Max.MAX, hex(Parse.parse(Max.MAX)));
      emit("parse", Nil.NIL.toUpperCase(), hex(Parse.parse(Nil.NIL.toUpperCase())));
      emit("parse", Max.MAX.toUpperCase(), hex(Parse.parse(Max.MAX.toUpperCase())));
    }

    {
      byte[] idBytes = Parse.parse("0f5abcd1-c194-47f3-905b-2df7263a084b");
      for (int off = 0; off <= 8; off++) {
        byte[] bytes = new byte[24];
        Arrays.fill(bytes, (byte) 0xaa);
        System.arraycopy(idBytes, 0, bytes, off, 16);
        emit("stringifyoff", off, hex(bytes), Stringify.stringify(bytes, off));
      }
    }

    // -----------------------------------------------------------------------
    // validate / version
    // -----------------------------------------------------------------------
    List<String> validateInputs =
        new ArrayList<>(
            List.of(
                Nil.NIL,
                Max.MAX,
                Nil.NIL.toUpperCase(),
                Max.MAX.toUpperCase(),
                "",
                "invalid uuid string",
                "00000000000000000000000000000000",
                "0f5abcd1-c194-47f3-905b-2df7263a084b",
                "0F5ABCD1-C194-47F3-905B-2DF7263A084B",
                "zyxwvuts-rqpo-nmlk-jihg-fedcba000000",
                "0f5abcd1-c194-47f3-905b-2df7263a084b\n",
                "\n0f5abcd1-c194-47f3-905b-2df7263a084b",
                "0f5abcd1-c194-47f3-905b-2df7263a084b ",
                "0f5abcd1-c194-07f3-905b-2df7263a084b",
                "0f5abcd1-c194-97f3-905b-2df7263a084b",
                "0f5abcd1-c194-47f3-705b-2df7263a084b",
                "0f5abcd1-c194-47f3-c05b-2df7263a084b"));
    for (int v = 1; v <= 8; v++) {
      for (String variant : new String[] {"8", "9", "a", "b", "0", "c", "f"}) {
        validateInputs.add("00000000-0000-" + v + "000-" + variant + "000-000000000000");
      }
    }
    for (String input : validateInputs) {
      emit("validate", jsonString(input), String.valueOf(Validate.validate(input)));
      String versionResult;
      try {
        versionResult = String.valueOf(Version.version(input));
      } catch (RuntimeException e) {
        versionResult = "THROW";
      }
      emit("version", jsonString(input), versionResult);
    }

    // -----------------------------------------------------------------------
    // State machines
    // -----------------------------------------------------------------------
    {
      byte[] rnds = {
        0, 0, 0, 0, 0, 0, 0, 0,
        (byte) 0x33, (byte) 0xc8,
        (byte) 0x9f, (byte) 0x68, (byte) 0xde, (byte) 0xce, (byte) 0xd8, (byte) 0x46
      };
      byte[] preNode = {
        (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc
      };
      List<V1State> stateSpecs =
          List.of(
              new V1State(),
              new V1State(10L, 20, 0x1234, preNode),
              new V1State(10L, 9999, 0x1234, preNode),
              new V1State(10L, 9998, 0x1234, preNode));
      long[] nows = {0, 9, 10, 11, 100};
      for (int s = 0; s < stateSpecs.size(); s++) {
        for (long now : nows) {
          V1State spec = stateSpecs.get(s);
          V1State state =
              new V1State(
                  spec.msecs(),
                  spec.nsecs(),
                  spec.clockseq(),
                  spec.node() == null ? null : spec.node().clone());
          V1State r = V1.updateV1State(state, now, rnds);
          emit(
              "updateV1State",
              s,
              now,
              r.msecs(),
              r.nsecs(),
              r.clockseq(),
              r.node() == null ? "null" : hex(r.node()));
        }
      }
    }

    {
      byte[] rnds = {
        (byte) 0x10, (byte) 0x91, (byte) 0x56, (byte) 0xbe,
        (byte) 0xc4, (byte) 0xfb, (byte) 0x0c, (byte) 0xc3,
        (byte) 0x18, (byte) 0xc4, (byte) 0x6c, (byte) 0x0c,
        (byte) 0x0c, (byte) 0x07, (byte) 0x39, (byte) 0x8f
      };
      List<V7State> stateSpecs =
          List.of(
              new V7State(),
              new V7State(1L, 123),
              new V7State(1L, 0xffffffff),
              new V7State(2L, 0xffffffff),
              new V7State(2L, -1),
              new V7State(100L, 0x7fffffff));
      long[] nows = {0, 1, 2, 3, 1000};
      for (int s = 0; s < stateSpecs.size(); s++) {
        for (long now : nows) {
          V7State spec = stateSpecs.get(s);
          V7State state = new V7State(spec.msecs(), spec.seq());
          V7State r = V7.updateV7State(state, now, rnds);
          emit("updateV7State", s, now, r.msecs(), r.seq());
        }
      }
    }
  }

  @Test
  @DisplayName("Java output is byte-identical to the TypeScript golden corpus")
  void matchesTypeScriptGolden() throws Exception {
    generate();

    List<String> expected = new ArrayList<>();
    InputStream raw = getClass().getResourceAsStream("/parity/ts_golden.txt.gz");
    assertNotNull(raw, "missing /parity/ts_golden.txt.gz");
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new GZIPInputStream(raw), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        expected.add(line);
      }
    }

    assertTrue(expected.size() > 3000, "golden corpus should be substantial");
    assertEquals(expected.size(), out.size(), "vector count differs from TypeScript");

    int mismatches = 0;
    StringBuilder report = new StringBuilder();
    for (int i = 0; i < expected.size(); i++) {
      if (!expected.get(i).equals(out.get(i))) {
        mismatches++;
        if (mismatches <= 20) {
          report
              .append("\n  line ")
              .append(i + 1)
              .append("\n    TS  : ")
              .append(expected.get(i))
              .append("\n    Java: ")
              .append(out.get(i));
        }
      }
    }
    assertEquals(0, mismatches, "Java/TypeScript divergence:" + report);
  }
}
