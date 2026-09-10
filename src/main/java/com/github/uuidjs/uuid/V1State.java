package com.github.uuidjs.uuid;

import java.util.Arrays;
import java.util.Objects;

/**
 * Port of the internal {@code V1State} type in {@code src/v1.ts}.
 *
 * <p>v1 &amp; v6 timestamps specify time from the Gregorian epoch in 100ns
 * intervals, requiring 57+ bits of precision — outside the precision of IEEE754
 * floats (i.e. JS numbers). The original works around this by storing
 * {@code msecs} (milliseconds since unix epoch) and {@code nsecs} (100-nanosecond
 * offset from {@code msecs}) separately. That representation is kept verbatim.
 *
 * <p>{@link #equals(Object)} is implemented because the TS tests compare whole
 * state objects with {@code assert.deepStrictEqual}.
 */
public final class V1State {

  /**
   * Sentinel for the TS initialiser {@code state.msecs ??= -Infinity}.
   *
   * <p>{@code msecs} is only ever compared ({@code ===}, {@code >}, {@code <})
   * while holding this value, and is unconditionally overwritten with {@code now}
   * before {@code updateV1State} returns — so it can never leak into a result.
   * {@link Long#MIN_VALUE} therefore reproduces {@code -Infinity} exactly for every
   * reachable code path.
   */
  static final long NEGATIVE_INFINITY = Long.MIN_VALUE;

  Long msecs;
  Integer nsecs;
  Integer clockseq;
  byte[] node;

  public V1State() {}

  public V1State(Long msecs, Integer nsecs, Integer clockseq, byte[] node) {
    this.msecs = msecs;
    this.nsecs = nsecs;
    this.clockseq = clockseq;
    this.node = node;
  }

  public Long msecs() {
    return msecs;
  }

  public Integer nsecs() {
    return nsecs;
  }

  public Integer clockseq() {
    return clockseq;
  }

  public byte[] node() {
    return node;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof V1State)) {
      return false;
    }
    V1State other = (V1State) o;
    return Objects.equals(msecs, other.msecs)
        && Objects.equals(nsecs, other.nsecs)
        && Objects.equals(clockseq, other.clockseq)
        && Arrays.equals(node, other.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(msecs, nsecs, clockseq, Arrays.hashCode(node));
  }

  @Override
  public String toString() {
    return "V1State{msecs="
        + msecs
        + ", nsecs="
        + nsecs
        + ", clockseq="
        + clockseq
        + ", node="
        + (node == null ? "null" : Arrays.toString(node))
        + '}';
  }
}
