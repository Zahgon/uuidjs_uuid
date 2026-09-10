package com.github.uuidjs.uuid;

import java.util.Locale;

/** Port of {@code src/stringify.ts}. */
public final class Stringify {
  private Stringify() {}

  /**
   * Mirrors the TS lookup table:
   *
   * <pre>
   * const byteToHex: string[] = [];
   * for (let i = 0; i &lt; 256; ++i) {
   *   byteToHex.push((i + 0x100).toString(16).slice(1));
   * }
   * </pre>
   */
  private static final String[] BYTE_TO_HEX = new String[256];

  static {
    for (int i = 0; i < 256; ++i) {
      BYTE_TO_HEX[i] = Integer.toHexString(i + 0x100).substring(1);
    }
  }

  /**
   * Reproduces a JavaScript out-of-bounds typed-array read.
   *
   * <p>In JS, {@code arr[16]} on a 15-element {@code Uint8Array} yields
   * {@code undefined}; {@code byteToHex[undefined]} is then also {@code undefined},
   * and string concatenation renders it as the literal text {@code "undefined"}.
   * That is what makes {@code stringify(BYTES.slice(0, 15))} produce an invalid
   * UUID string and therefore throw {@code TypeError} rather than an index error.
   * Java would throw {@link ArrayIndexOutOfBoundsException}, so the read is
   * emulated explicitly here.
   */
  private static String hexAt(byte[] arr, int index) {
    if (index < 0 || index >= arr.length) {
      return "undefined";
    }
    return BYTE_TO_HEX[arr[index] & 0xff];
  }

  /** Mirrors {@code unsafeStringify(arr, offset = 0)}. */
  public static String unsafeStringify(byte[] arr) {
    return unsafeStringify(arr, 0);
  }

  /**
   * Mirrors:
   *
   * <pre>
   * export function unsafeStringify(arr: Uint8Array, offset = 0): string {
   *   return (byteToHex[arr[offset + 0]] + ... ).toLowerCase();
   * }
   * </pre>
   *
   * <p>The trailing {@code toLowerCase()} is preserved verbatim — see the upstream
   * comment "No, you can't remove the toLowerCase() call".
   */
  public static String unsafeStringify(byte[] arr, int offset) {
    String s =
        hexAt(arr, offset + 0)
            + hexAt(arr, offset + 1)
            + hexAt(arr, offset + 2)
            + hexAt(arr, offset + 3)
            + '-'
            + hexAt(arr, offset + 4)
            + hexAt(arr, offset + 5)
            + '-'
            + hexAt(arr, offset + 6)
            + hexAt(arr, offset + 7)
            + '-'
            + hexAt(arr, offset + 8)
            + hexAt(arr, offset + 9)
            + '-'
            + hexAt(arr, offset + 10)
            + hexAt(arr, offset + 11)
            + hexAt(arr, offset + 12)
            + hexAt(arr, offset + 13)
            + hexAt(arr, offset + 14)
            + hexAt(arr, offset + 15);
    return s.toLowerCase(Locale.ROOT);
  }

  /** Mirrors {@code stringify(arr, offset = 0)}. */
  public static String stringify(byte[] arr) {
    return stringify(arr, 0);
  }

  /**
   * Mirrors:
   *
   * <pre>
   * function stringify(arr: Uint8Array, offset = 0) {
   *   const uuid = unsafeStringify(arr, offset);
   *   if (!validate(uuid)) {
   *     throw TypeError('Stringified UUID is invalid');
   *   }
   *   return uuid;
   * }
   * </pre>
   */
  public static String stringify(byte[] arr, int offset) {
    String uuid = unsafeStringify(arr, offset);

    // Consistency check for valid UUID. If this throws, it's likely due to one
    // of the following:
    // - One or more input array values don't map to a hex octet (leading to
    //   "undefined" in the uuid)
    // - Invalid input values for the RFC `version` or `variant` fields
    if (!Validate.validate(uuid)) {
      throw new JSTypeError("Stringified UUID is invalid");
    }

    return uuid;
  }
}
