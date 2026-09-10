package com.github.uuidjs.uuid;

/** Port of {@code src/v5.ts}. */
public final class V5 {
  private V5() {}

  /** Mirrors {@code v5.DNS} / re-export of {@code DNS} from v35. */
  public static final String DNS = V35.DNS;

  /** Mirrors {@code v5.URL} / re-export of {@code URL} from v35. */
  public static final String URL = V35.URL;

  private static final int VERSION = 0x50;

  public static String v5(String value, String namespace) {
    return (String) V35.v35(VERSION, Sha1::sha1, value, namespace, null, null);
  }

  public static String v5(String value, byte[] namespace) {
    return (String) V35.v35(VERSION, Sha1::sha1, value, namespace, null, null);
  }

  public static String v5(byte[] value, String namespace) {
    return (String) V35.v35(VERSION, Sha1::sha1, value, namespace, null, null);
  }

  public static String v5(byte[] value, byte[] namespace) {
    return (String) V35.v35(VERSION, Sha1::sha1, value, namespace, null, null);
  }

  public static byte[] v5(String value, String namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, 0);
  }

  public static byte[] v5(String value, String namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, offset);
  }

  public static byte[] v5(String value, byte[] namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, 0);
  }

  public static byte[] v5(String value, byte[] namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, offset);
  }

  public static byte[] v5(byte[] value, String namespace, byte[] buf) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, 0);
  }

  public static byte[] v5(byte[] value, String namespace, byte[] buf, int offset) {
    return (byte[]) V35.v35(VERSION, Sha1::sha1, value, namespace, buf, offset);
  }
}
