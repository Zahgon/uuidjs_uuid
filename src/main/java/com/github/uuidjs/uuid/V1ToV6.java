package com.github.uuidjs.uuid;

/** Port of {@code src/v1ToV6.ts}. */
public final class V1ToV6 {
  private V1ToV6() {}

  /** Convert a v1 UUID string to a v6 UUID string. */
  public static String v1ToV6(String uuid) {
    return Stringify.unsafeStringify(convert(Parse.parse(uuid)), 0);
  }

  /** Convert v1 UUID bytes to v6 UUID bytes. */
  public static byte[] v1ToV6(byte[] uuid) {
    return convert(uuid);
  }

  /** Do the field transformation needed for v1 -> v6. */
  private static byte[] convert(byte[] v1Bytes) {
    return new byte[] {
      (byte) (((v1Bytes[6] & 0x0f) << 4) | (((v1Bytes[7] & 0xff) >> 4) & 0x0f)),
      (byte) (((v1Bytes[7] & 0x0f) << 4) | ((v1Bytes[4] & 0xf0) >> 4)),
      (byte) (((v1Bytes[4] & 0x0f) << 4) | ((v1Bytes[5] & 0xf0) >> 4)),
      (byte) (((v1Bytes[5] & 0x0f) << 4) | ((v1Bytes[0] & 0xf0) >> 4)),
      (byte) (((v1Bytes[0] & 0x0f) << 4) | ((v1Bytes[1] & 0xf0) >> 4)),
      (byte) (((v1Bytes[1] & 0x0f) << 4) | ((v1Bytes[2] & 0xf0) >> 4)),
      (byte) (0x60 | (v1Bytes[2] & 0x0f)),
      v1Bytes[3],
      v1Bytes[8],
      v1Bytes[9],
      v1Bytes[10],
      v1Bytes[11],
      v1Bytes[12],
      v1Bytes[13],
      v1Bytes[14],
      v1Bytes[15],
    };
  }
}
