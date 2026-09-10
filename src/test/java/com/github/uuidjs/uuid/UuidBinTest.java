package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the CLI ({@code src/uuid-bin.ts}).
 *
 * <p>The original has no test coverage for its CLI at all. Expected values here were
 * captured by running the real {@code node dist-node/bin/uuid ...}.
 */
@DisplayName("uuid-bin CLI")
class UuidBinTest {

  /** Runs the CLI, returning {@code [exitCode, stdout]}. */
  private static Object[] run(String... args) {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    try {
      System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
      int code = UuidBin.run(args);
      return new Object[] {code, captured.toString(StandardCharsets.UTF_8)};
    } finally {
      System.setOut(originalOut);
    }
  }

  private static final String EXPECTED_USAGE =
      "Usage:\n"
          + "  uuid\n"
          + "  uuid v1\n"
          + "  uuid v3 <name> <namespace uuid>\n"
          + "  uuid v4\n"
          + "  uuid v5 <name> <namespace uuid>\n"
          + "  uuid v6\n"
          + "  uuid v7\n"
          + "  uuid --help\n"
          + "\n"
          + "Note: <namespace uuid> may be \"URL\" or \"DNS\" to use the corresponding UUIDs"
          + " defined by RFC9562\n";

  @Test
  @DisplayName("--help prints usage and exits 0")
  void help() {
    Object[] r = run("--help");
    assertEquals(0, r[0]);
    assertEquals(EXPECTED_USAGE, r[1]);
  }

  @Test
  @DisplayName("unknown command prints usage and exits 1")
  void unknownCommand() {
    Object[] r = run("nope");
    assertEquals(1, r[0]);
    assertEquals(EXPECTED_USAGE, r[1]);
  }

  @Test
  @DisplayName("v3 with DNS/URL keywords matches the TypeScript CLI")
  void v3Keywords() {
    // Captured from: node dist-node/bin/uuid v3 hello.example.com DNS
    assertEquals("9125a8dc-52ee-365b-a5aa-81b0b3681cf6\n", run("v3", "hello.example.com", "DNS")[1]);
    // node dist-node/bin/uuid v3 http://example.com/hello URL
    assertEquals(
        "c6235813-3ba4-3801-ae84-e0a6ebb7d138\n",
        run("v3", "http://example.com/hello", "URL")[1]);
    // explicit namespace uuid (no keyword substitution)
    assertEquals(
        "a981a0c2-68b1-35dc-bcfc-296e52ab01ec\n",
        run("v3", "hello", "0f5abcd1-c194-47f3-905b-2df7263a084b")[1]);
  }

  @Test
  @DisplayName("v5 with DNS/URL keywords matches the TypeScript CLI")
  void v5Keywords() {
    assertEquals("fdda765f-fc57-5604-a269-52a7df8164ec\n", run("v5", "hello.example.com", "DNS")[1]);
    assertEquals(
        "3bbcee75-cecc-5b56-8031-b6641c1ed1f1\n",
        run("v5", "http://example.com/hello", "URL")[1]);
    assertEquals(
        "90123e1c-7512-523e-bb28-76fab9f2f73d\n",
        run("v5", "hello", "0f5abcd1-c194-47f3-905b-2df7263a084b")[1]);
  }

  @Test
  @DisplayName("v1/v4/v6/v7 emit a single valid UUID of the right version")
  void generatorSubcommands() {
    for (String[] spec : new String[][] {{"v1", "1"}, {"v4", "4"}, {"v6", "6"}, {"v7", "7"}}) {
      Object[] r = run(spec[0]);
      assertEquals(0, r[0]);
      String out = (String) r[1];
      assertTrue(out.endsWith("\n"), spec[0] + " should print a trailing newline");
      String id = out.trim();
      assertEquals(36, id.length(), spec[0] + " length");
      assertTrue(Validate.validate(id), spec[0] + " should be a valid UUID: " + id);
      assertEquals(Integer.parseInt(spec[1]), Version.version(id), spec[0] + " version");
    }
  }

  @Test
  @DisplayName("no arguments defaults to v4")
  void defaultsToV4() {
    Object[] r = run();
    assertEquals(0, r[0]);
    String id = ((String) r[1]).trim();
    assertTrue(Validate.validate(id));
    assertEquals(4, Version.version(id));
  }

  @Test
  @DisplayName("an empty first argument also defaults to v4 (JS falsy coalescing)")
  void emptyArgumentDefaultsToV4() {
    // TS: `args.shift() || 'v4'` — '' is falsy, so `uuid ""` behaves like `uuid`.
    // Verified against: node dist-node/bin/uuid "" -> a v4 UUID, exit 0.
    Object[] r = run("");
    assertEquals(0, r[0]);
    String id = ((String) r[1]).trim();
    assertTrue(Validate.validate(id), "expected a UUID, got: " + id);
    assertEquals(4, Version.version(id));
  }

  @Test
  @DisplayName("v3/v5 assert when name or namespace is missing")
  void missingArguments() {
    // TS: assert.ok(name != null, 'v3 name not specified')
    assertThrows(AssertionError.class, () -> UuidBin.run(new String[] {"v3"}));
    assertThrows(AssertionError.class, () -> UuidBin.run(new String[] {"v3", "name-only"}));
    assertThrows(AssertionError.class, () -> UuidBin.run(new String[] {"v5"}));
    assertThrows(AssertionError.class, () -> UuidBin.run(new String[] {"v5", "name-only"}));
  }

  @Test
  @DisplayName("--help takes precedence over a version argument")
  void helpTakesPrecedence() {
    // TS: `args.indexOf('--help') >= 0` is checked before the version is shifted.
    Object[] r = run("v1", "--help");
    assertEquals(0, r[0]);
    assertEquals(EXPECTED_USAGE, r[1]);
  }
}
