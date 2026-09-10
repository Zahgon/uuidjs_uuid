package com.github.uuidjs.uuid;

import java.security.SecureRandom;

/** Port of {@code src/rng.ts}. */
public final class Rng {
  private Rng() {}

  /**
   * Mirrors the module-level shared buffer:
   *
   * <pre>
   * const rnds8 = new Uint8Array(16);
   * export default function rng() {
   *   return crypto.getRandomValues(rnds8);
   * }
   * </pre>
   *
   * <p>The buffer really is shared and re-used between calls in the original — that
   * is an observable side effect (callers such as {@code _v4} mutate the returned
   * array in place), so it is preserved rather than "fixed".
   */
  private static final byte[] RNDS8 = new byte[16];

  /** {@code crypto.getRandomValues} requires a cryptographically strong source. */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public static byte[] rng() {
    SECURE_RANDOM.nextBytes(RNDS8);
    return RNDS8;
  }
}
