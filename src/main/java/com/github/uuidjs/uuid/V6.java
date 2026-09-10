package com.github.uuidjs.uuid;

/** Port of {@code src/v6.ts}. */
public final class V6 {
  private V6() {}

  public static String v6() {
    return v6((Version1Options) null);
  }

  public static String v6(Version1Options options) {
    return Stringify.unsafeStringify(v6Bytes(options), 0);
  }

  public static byte[] v6(Version1Options options, byte[] buf) {
    return v6(options, buf, 0);
  }

  public static byte[] v6(Version1Options options, byte[] buf, int offset) {
    byte[] bytes = v6Bytes(options);

    // Return as a byte array if requested
    if (buf != null) {
      if (offset < 0 || offset + 16 > buf.length) {
        throw new JSRangeError(
            "UUID byte range " + offset + ":" + (offset + 15) + " is out of buffer bounds");
      }

      for (int i = 0; i < 16; i++) {
        buf[offset + i] = bytes[i];
      }
      return buf;
    }

    return bytes;
  }

  /**
   * Mirrors the shared body of the TS {@code v6()}:
   *
   * <pre>
   * options ??= {};
   * let bytes = v1({ ...options, _v6: true }, new Uint8Array(16));
   * bytes = v1ToV6(bytes);
   * </pre>
   *
   * <p>v6 is v1 with a different field layout, so it starts from a v1 UUID — albeit
   * with slightly different behavior around how the clock_seq and node fields are
   * randomized, which is why v1 is called with {@code _v6: true}.
   */
  private static byte[] v6Bytes(Version1Options options) {
    Version1Options merged = (options == null) ? new Version1Options() : options.copy();
    merged.v6(true);

    byte[] bytes = V1.generate(merged, new byte[16], 0);

    // Reorder the fields to v6 layout.
    return V1ToV6.v1ToV6(bytes);
  }
}
