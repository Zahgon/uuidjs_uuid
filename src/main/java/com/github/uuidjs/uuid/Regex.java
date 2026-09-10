package com.github.uuidjs.uuid;

import java.util.regex.Pattern;

/** Port of {@code src/regex.ts}. */
final class Regex {
  private Regex() {}

  /**
   * Mirrors the TS default export:
   *
   * <pre>
   * /^(?:[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}|00000000-0000-0000-0000-000000000000|ffffffff-ffff-ffff-ffff-ffffffffffff)$/i
   * </pre>
   *
   * <p>Note: the pattern is always applied with {@link java.util.regex.Matcher#matches()}.
   * JavaScript's {@code $} (without the {@code m} flag) anchors at the very end of
   * input, whereas Java's {@code $} also matches immediately before a final line
   * terminator. Using {@code matches()} requires the whole input to be consumed and
   * therefore reproduces the JavaScript semantics exactly.
   */
  static final Pattern REGEX =
      Pattern.compile(
          "^(?:[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
              + "|00000000-0000-0000-0000-000000000000"
              + "|ffffffff-ffff-ffff-ffff-ffffffffffff)$",
          Pattern.CASE_INSENSITIVE);
}
