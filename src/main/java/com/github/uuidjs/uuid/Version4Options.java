package com.github.uuidjs.uuid;

import java.util.function.Supplier;

/**
 * Port of the TS {@code Version4Options} type:
 *
 * <pre>
 * export type Version4Options = {
 *   random?: Uint8Array;
 *   rng?: () =&gt; Uint8Array;
 * };
 * </pre>
 */
public final class Version4Options {

  private byte[] random;
  private Supplier<byte[]> rng;

  public Version4Options() {}

  public Version4Options random(byte[] random) {
    this.random = random;
    return this;
  }

  public Version4Options rng(Supplier<byte[]> rng) {
    this.rng = rng;
    return this;
  }

  byte[] random() {
    return random;
  }

  Supplier<byte[]> rng() {
    return rng;
  }
}
