package com.github.uuidjs.uuid;

import java.util.Objects;

/**
 * Port of the internal {@code V7State} type in {@code src/v7.ts}.
 *
 * <p>{@code seq} is a 32-bit sequence number. The original relies on JavaScript's
 * {@code | 0} to force 32-bit wraparound; Java's {@code int} wraps natively, so the
 * arithmetic maps across without a mask. Note that a JS {@code 0xffffffff}
 * (4294967295) becomes {@code -1} as a Java {@code int} — the bit pattern, and
 * therefore every downstream shift and comparison, is identical.
 */
public final class V7State {

  /** Sentinel for {@code state.msecs ??= -Infinity}. See {@link V1State#NEGATIVE_INFINITY}. */
  static final long NEGATIVE_INFINITY = Long.MIN_VALUE;

  Long msecs;
  Integer seq;

  public V7State() {}

  public V7State(Long msecs, Integer seq) {
    this.msecs = msecs;
    this.seq = seq;
  }

  public Long msecs() {
    return msecs;
  }

  public Integer seq() {
    return seq;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof V7State)) {
      return false;
    }
    V7State other = (V7State) o;
    return Objects.equals(msecs, other.msecs) && Objects.equals(seq, other.seq);
  }

  @Override
  public int hashCode() {
    return Objects.hash(msecs, seq);
  }

  @Override
  public String toString() {
    return "V7State{msecs=" + msecs + ", seq=" + seq + '}';
  }
}
