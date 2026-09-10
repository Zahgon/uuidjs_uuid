package com.github.uuidjs.uuid;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Port of {@code src/uuid-bin.ts} (the {@code uuid} CLI declared in
 * {@code package.json}'s {@code bin} field).
 *
 * <p>{@code process.exit(0)} / {@code process.exit(1)} become
 * {@link System#exit(int)}, and {@code assert.ok(x != null, msg)} becomes an
 * explicit check that throws — Node's {@code assert} module aborts the process with
 * a non-zero status, which an uncaught exception also does on the JVM.
 */
public final class UuidBin {
  private UuidBin() {}

  static void usage() {
    System.out.println("Usage:");
    System.out.println("  uuid");
    System.out.println("  uuid v1");
    System.out.println("  uuid v3 <name> <namespace uuid>");
    System.out.println("  uuid v4");
    System.out.println("  uuid v5 <name> <namespace uuid>");
    System.out.println("  uuid v6");
    System.out.println("  uuid v7");
    System.out.println("  uuid --help");
    System.out.println(
        "\nNote: <namespace uuid> may be \"URL\" or \"DNS\" to use the corresponding UUIDs"
            + " defined by RFC9562");
  }

  public static void main(String[] argv) {
    System.exit(run(argv));
  }

  /**
   * The CLI body, returning the process exit code instead of calling
   * {@link System#exit(int)} directly, so it can be exercised by tests. {@code main}
   * is a thin wrapper; observable behaviour (stdout and exit status) is unchanged.
   */
  static int run(String[] argv) {
    Deque<String> args = new ArrayDeque<>(Arrays.asList(argv));

    if (args.contains("--help")) {
      usage();
      return 0;
    }

    // Mirrors `const version = args.shift() || 'v4';`
    //
    // NOTE: this is JavaScript *falsy* coalescing, not a null check. `args.shift()`
    // yields `undefined` when there are no arguments, but it yields `''` when the
    // first argument is an empty string — and `'' || 'v4'` is also `'v4'`. So
    // `uuid ""` generates a v4 UUID rather than printing usage. A plain
    // "is the deque empty" test gets the empty-string case wrong.
    String version = args.pollFirst();
    if (version == null || version.isEmpty()) {
      version = "v4";
    }

    switch (version) {
      case "v1":
        System.out.println(V1.v1());
        break;

      case "v3": {
        String name = args.pollFirst();
        String namespace = args.pollFirst();

        assertOk(name != null, "v3 name not specified");
        assertOk(namespace != null, "v3 namespace not specified");

        if ("URL".equals(namespace)) {
          namespace = V3.URL;
        }

        if ("DNS".equals(namespace)) {
          namespace = V3.DNS;
        }

        System.out.println(V3.v3(name, namespace));
        break;
      }

      case "v4":
        System.out.println(V4.v4());
        break;

      case "v5": {
        String name = args.pollFirst();
        String namespace = args.pollFirst();

        assertOk(name != null, "v5 name not specified");
        assertOk(namespace != null, "v5 namespace not specified");

        if ("URL".equals(namespace)) {
          namespace = V5.URL;
        }

        if ("DNS".equals(namespace)) {
          namespace = V5.DNS;
        }

        System.out.println(V5.v5(name, namespace));
        break;
      }

      case "v6":
        System.out.println(V6.v6());
        break;

      case "v7":
        System.out.println(V7.v7());
        break;

      default:
        usage();
        return 1;
    }

    return 0;
  }

  /** Mirrors {@code assert.ok(value, message)} from {@code node:assert/strict}. */
  private static void assertOk(boolean value, String message) {
    if (!value) {
      throw new AssertionError(message);
    }
  }
}
