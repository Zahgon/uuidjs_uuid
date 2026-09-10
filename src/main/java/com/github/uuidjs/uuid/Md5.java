package com.github.uuidjs.uuid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Port of {@code src/md5.ts}.
 *
 * <p>The TS implementation delegates to Node's {@code crypto.createHash('md5')};
 * the Java equivalent is {@link MessageDigest}. The upstream repo also ships a
 * hand-written pure-JS fallback ({@code md5-browser.ts}) purely because browsers
 * lack a synchronous digest API. That file has no Java counterpart by design —
 * {@link MessageDigest} is available everywhere the JVM is.
 */
public final class Md5 {
  private Md5() {}

  public static byte[] md5(byte[] bytes) {
    try {
      return MessageDigest.getInstance("MD5").digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 not available", e);
    }
  }
}
