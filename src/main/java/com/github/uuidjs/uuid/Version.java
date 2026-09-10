package com.github.uuidjs.uuid;

/** Port of {@code src/version.ts}. */
public final class Version {
  private Version() {}

  /**
   * Mirrors:
   *
   * <pre>
   * function version(uuid: string) {
   *   if (!validate(uuid)) {
   *     throw TypeError('Invalid UUID');
   *   }
   *   return parseInt(uuid.slice(14, 15), 16);
   * }
   * </pre>
   */
  public static int version(Object uuid) {
    if (!Validate.validate(uuid)) {
      throw new JSTypeError("Invalid UUID");
    }
    String s = (String) uuid;
    return Integer.parseInt(s.substring(14, 15), 16);
  }
}
