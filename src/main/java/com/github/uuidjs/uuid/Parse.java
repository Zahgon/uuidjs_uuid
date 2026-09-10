package com.github.uuidjs.uuid;

/** Port of {@code src/parse.ts}. */
public final class Parse {
  private Parse() {}

  /**
   * Mirrors:
   *
   * <pre>
   * function parse(uuid: string): NonSharedArrayBuffer {
   *   if (!validate(uuid)) {
   *     throw TypeError('Invalid UUID');
   *   }
   *   ...
   * }
   * </pre>
   *
   * <p>Numeric notes:
   *
   * <ul>
   *   <li>{@code parseInt(slice, 16)} becomes {@link Long#parseLong(String, int)}. A
   *       {@code long} is required for the final 12-hex-digit (48-bit) field, which
   *       overflows {@code int}.
   *   <li>JavaScript {@code >>>} coerces its operand with {@code ToUint32} first, so
   *       {@code v >>> 24} on a 48-bit {@code v} keeps only bits 24..31. Masking the
   *       {@code long} result with {@code 0xff} reproduces that exactly.
   *   <li>The two highest bytes use {@code /} in the original ("Use '/' to avoid
   *       32-bit truncation when bit-shifting high-order bytes"). Because {@code v} is
   *       non-negative, JavaScript's float division followed by {@code ToInt32}
   *       truncation is identical to Java's integer division.
   * </ul>
   */
  public static byte[] parse(Object uuid) {
    if (!Validate.validate(uuid)) {
      throw new JSTypeError("Invalid UUID");
    }

    String s = (String) uuid;
    byte[] out = new byte[16];
    long v;

    // Parse ########-....-....-....-............
    v = Long.parseLong(s.substring(0, 8), 16);
    out[0] = (byte) (v >>> 24);
    out[1] = (byte) ((v >>> 16) & 0xff);
    out[2] = (byte) ((v >>> 8) & 0xff);
    out[3] = (byte) (v & 0xff);

    // Parse ........-####-....-....-............
    v = Long.parseLong(s.substring(9, 13), 16);
    out[4] = (byte) (v >>> 8);
    out[5] = (byte) (v & 0xff);

    // Parse ........-....-####-....-............
    v = Long.parseLong(s.substring(14, 18), 16);
    out[6] = (byte) (v >>> 8);
    out[7] = (byte) (v & 0xff);

    // Parse ........-....-....-####-............
    v = Long.parseLong(s.substring(19, 23), 16);
    out[8] = (byte) (v >>> 8);
    out[9] = (byte) (v & 0xff);

    // Parse ........-....-....-....-############
    // (Use "/" to avoid 32-bit truncation when bit-shifting high-order bytes)
    v = Long.parseLong(s.substring(24, 36), 16);
    out[10] = (byte) ((v / 0x10000000000L) & 0xff);
    out[11] = (byte) ((v / 0x100000000L) & 0xff);
    out[12] = (byte) ((v >>> 24) & 0xff);
    out[13] = (byte) ((v >>> 16) & 0xff);
    out[14] = (byte) ((v >>> 8) & 0xff);
    out[15] = (byte) (v & 0xff);

    return out;
  }
}
