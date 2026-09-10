package com.github.uuidjs.uuid;

/** Port of {@code src/validate.ts}. */
public final class Validate {
  private Validate() {}

  /**
   * Mirrors:
   *
   * <pre>
   * function validate(uuid: unknown) {
   *   return typeof uuid === 'string' &amp;&amp; REGEX.test(uuid);
   * }
   * </pre>
   *
   * <p>The parameter is {@link Object} (not {@link String}) because the TS signature
   * is {@code unknown} and the test suite deliberately feeds it {@code null},
   * numbers, dates, regexes and booleans — all of which must yield {@code false}
   * rather than throwing.
   */
  public static boolean validate(Object uuid) {
    return uuid instanceof String && Regex.REGEX.matcher((String) uuid).matches();
  }
}
