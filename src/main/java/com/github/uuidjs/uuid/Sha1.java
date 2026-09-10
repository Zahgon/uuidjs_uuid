package com.github.uuidjs.uuid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Port of {@code src/sha1.ts}.
 *
 * <p>See {@link Md5} for why {@code sha1-browser.ts} has no Java counterpart.
 */
public final class Sha1 {
  private Sha1() {}

  public static byte[] sha1(byte[] bytes) {
    try {
      return MessageDigest.getInstance("SHA-1").digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-1 not available", e);
    }
  }
}
