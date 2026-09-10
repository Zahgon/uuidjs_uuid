package com.github.uuidjs.uuid;

/** Port of {@code src/v3.ts}. */
public final class V3 {
  private V3() {}

  /** Mirrors {@code v3.DNS} / re-export of {@code DNS} from v35. */
  public static final String DNS = V35.DNS;

  /** Mirrors {@code v3.URL} / re-export of {@code URL} from v35. */
  public static final String URL = V35.URL;

  private static final int VERSION = 0x30;

  public static String v3(String value, String namespace) {
    return (String) V35.v35(VERSION, Md5::md5, value, namespace, null, null);
  }

  public static String v3(String value, byte[] namespace) {
    return (String) V35.v35(VERSION, Md5::md5, value, namespace, null, null);
  }

  public static String v3(byte[] value, String namespace) {
    return (String) V35.v35(VERSION, Md5::md5, value, namespace, null, null);
  }

  public static String v3(byte[] value, byte[] namespace) {
    return (String) V35.v35(VERSION, Md5::md5, value, namespace, null, null);
  }

  public static byte[] v3(String value, String namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, 0);
  }

  public static byte[] v3(String value, String namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, offset);
  }

  public static byte[] v3(String value, byte[] namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, 0);
  }

  public static byte[] v3(String value, byte[] namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, offset);
  }

  public static byte[] v3(byte[] value, String namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, 0);
  }

  public static byte[] v3(byte[] value, String namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Md5::md5, value, namespace, buf, offset);
  }
}
