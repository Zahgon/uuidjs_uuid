package com.github.uuidjs.uuid;

import java.util.function.Supplier;

/**
 * Port of the TS {@code Version7Options} type:
 *
 * <pre>
 * export type Version7Options = {
 *   random?: Uint8Array;
 *   msecs?: number;
 *   seq?: number;
 *   rng?: () =&gt; Uint8Array;
 * };
 * </pre>
 */
public final class Version7Options {

  private byte[] random;
  private Long msecs;
  private Integer seq;
  private Supplier<byte[]> rng;

  public Version7Options() {}

  public Version7Options random(byte[] random) {
    this.random = random;
    return this;
  }

  public Version7Options msecs(Long msecs) {
    this.msecs = msecs;
    return this;
  }

  public Version7Options msecs(long msecs) {
    return msecs(Long.valueOf(msecs));
  }

  public Version7Options seq(Integer seq) {
    this.seq = seq;
    return this;
  }

  public Version7Options seq(int seq) {
    return seq(Integer.valueOf(seq));
  }

  public Version7Options rng(Supplier<byte[]> rng) {
    this.rng = rng;
    return this;
  }

  byte[] random() {
    return random;
  }

  Long msecs() {
    return msecs;
  }

  Integer seq() {
    return seq;
  }

  Supplier<byte[]> rng() {
    return rng;
  }
}
