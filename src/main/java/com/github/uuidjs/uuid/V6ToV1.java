package com.github.uuidjs.uuid;

/** Port of {@code src/v6ToV1.ts}. */
public final class V6ToV1 {
  private V6ToV1() {}

  /** Convert a v6 UUID string to a v1 UUID string. */
  public static String v6ToV1(String uuid) {
    return Stringify.unsafeStringify(convert(Parse.parse(uuid)), 0);
  }

  /** Convert v6 UUID bytes to v1 UUID bytes. */
  public static byte[] v6ToV1(byte[] uuid) {
    return convert(uuid);
  }

  /** Do the field transformation needed for v6 -> v1. */
  private static byte[] convert(byte[] v6Bytes) {
    return new byte[] {
      (byte) (((v6Bytes[3] & 0x0f) << 4) | (((v6Bytes[4] & 0xff) >> 4) & 0x0f)),
      (byte) (((v6Bytes[4] & 0x0f) << 4) | ((v6Bytes[5] & 0xf0) >> 4)),
      (byte) (((v6Bytes[5] & 0x0f) << 4) | (v6Bytes[6] & 0x0f)),
      v6Bytes[7],
      (byte) (((v6Bytes[1] & 0x0f) << 4) | ((v6Bytes[2] & 0xf0) >> 4)),
      (byte) (((v6Bytes[2] & 0x0f) << 4) | ((v6Bytes[3] & 0xf0) >> 4)),
      (byte) (0x10 | ((v6Bytes[0] & 0xf0) >> 4)),
      (byte) (((v6Bytes[0] & 0x0f) << 4) | ((v6Bytes[1] & 0xf0) >> 4)),
      v6Bytes[8],
      v6Bytes[9],
      v6Bytes[10],
      v6Bytes[11],
      v6Bytes[12],
      v6Bytes[13],
      v6Bytes[14],
      v6Bytes[15],
    };
  }
}
