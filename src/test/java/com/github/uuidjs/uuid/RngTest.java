package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/rng.test.ts}. */
@DisplayName("rng")
class RngTest {

  @Test
  @DisplayName("Node.js RNG")
  void nodeRng() {
    byte[] bytes = Rng.rng();
    assertEquals(16, bytes.length);

    // The TS assertion here is `assert.equal(typeof bytes[i], 'number')`, which is
    // vacuous for a Uint8Array — it cannot fail. Translating it literally
    // (e.g. "every element is in 0..255") would be equally vacuous in Java, since
    // `b & 0xff` is in 0..255 by construction.
    //
    // Instead we assert what that test is *morally* checking: that rng() actually
    // produces entropy. This would fail against a stub returning a constant
    // buffer, whereas the literal translation would not.
    Set<Integer> distinct = new HashSet<>();
    boolean anyNonZero = false;
    for (int call = 0; call < 200; call++) {
      byte[] b = Rng.rng();
      assertEquals(16, b.length, "rng() must always yield 16 bytes");
      for (byte value : b) {
        distinct.add(value & 0xff);
        if (value != 0) {
          anyNonZero = true;
        }
      }
    }

    assertTrue(anyNonZero, "rng() produced only zero bytes across 200 calls");
    // 3200 sampled bytes: seeing fewer than 64 of the 256 possible values would
    // indicate a broken source. The probability of a false failure is negligible.
    assertTrue(
        distinct.size() >= 64,
        "rng() entropy too low: only " + distinct.size() + " distinct byte values in 3200 samples");
  }

  // Test of whatwgRNG missing for now since with esmodules we can no longer
  // manipulate the require.cache.
}
