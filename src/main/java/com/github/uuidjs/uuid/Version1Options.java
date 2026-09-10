package com.github.uuidjs.uuid;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Port of the TS {@code Version1Options} type:
 *
 * <pre>
 * export type Version1Options = {
 *   node?: Uint8Array;
 *   clockseq?: number;
 *   random?: Uint8Array;
 *   rng?: () =&gt; Uint8Array;
 *   msecs?: number;
 *   nsecs?: number;
 *   _v6?: boolean; // Internal use only!
 * };
 * </pre>
 *
 * <p><b>Why key presence is tracked.</b> {@code v1()} contains this branch:
 *
 * <pre>
 * const optionsKeys = Object.keys(options);
 * if (optionsKeys.length === 1 &amp;&amp; optionsKeys[0] === '_v6') {
 *   options = undefined;
 * }
 * </pre>
 *
 * <p>That is a reflective check over the object's own enumerable keys, which has no
 * direct Java analogue. A plain "all other fields are null" test would be *almost*
 * right, but would wrongly collapse an explicitly-present-but-undefined key (e.g.
 * {@code {msecs: undefined, _v6: true}} has two keys in JS). Recording which
 * setters were called reproduces {@code Object.keys()} faithfully.
 */
public class Version1Options {

  private final Set<String> presentKeys = new LinkedHashSet<>();

  private byte[] node;
  private Integer clockseq;
  private byte[] random;
  private Supplier<byte[]> rng;
  private Long msecs;
  private Integer nsecs;
  private boolean v6;

  public Version1Options() {}

  // --- fluent setters (each records the key, mirroring JS object literal keys) ---

  public Version1Options node(byte[] node) {
    this.node = node;
    presentKeys.add("node");
    return this;
  }

  public Version1Options clockseq(Integer clockseq) {
    this.clockseq = clockseq;
    presentKeys.add("clockseq");
    return this;
  }

  public Version1Options random(byte[] random) {
    this.random = random;
    presentKeys.add("random");
    return this;
  }

  public Version1Options rng(Supplier<byte[]> rng) {
    this.rng = rng;
    presentKeys.add("rng");
    return this;
  }

  public Version1Options msecs(Long msecs) {
    this.msecs = msecs;
    presentKeys.add("msecs");
    return this;
  }

  public Version1Options msecs(long msecs) {
    return msecs(Long.valueOf(msecs));
  }

  public Version1Options nsecs(Integer nsecs) {
    this.nsecs = nsecs;
    presentKeys.add("nsecs");
    return this;
  }

  public Version1Options nsecs(int nsecs) {
    return nsecs(Integer.valueOf(nsecs));
  }

  /** Internal use only — mirrors the {@code _v6} flag. */
  Version1Options v6(boolean v6) {
    this.v6 = v6;
    presentKeys.add("_v6");
    return this;
  }

  // --- accessors ---

  byte[] node() {
    return node;
  }

  Integer clockseq() {
    return clockseq;
  }

  byte[] random() {
    return random;
  }

  Supplier<byte[]> rng() {
    return rng;
  }

  Long msecs() {
    return msecs;
  }

  Integer nsecs() {
    return nsecs;
  }

  boolean isV6() {
    return v6;
  }

  /** Mirrors {@code Object.keys(options)}. */
  Set<String> keys() {
    return presentKeys;
  }

  /** Mirrors the object spread {@code {...options, _v6: true}} used by {@code v6()}. */
  Version1Options copy() {
    Version1Options c = new Version1Options();
    for (String key : presentKeys) {
      switch (key) {
        case "node" -> c.node(node);
        case "clockseq" -> c.clockseq(clockseq);
        case "random" -> c.random(random);
        case "rng" -> c.rng(rng);
        case "msecs" -> c.msecs(msecs);
        case "nsecs" -> c.nsecs(nsecs);
        case "_v6" -> c.v6(v6);
        default -> throw new IllegalStateException("unknown key: " + key);
      }
    }
    return c;
  }
}
