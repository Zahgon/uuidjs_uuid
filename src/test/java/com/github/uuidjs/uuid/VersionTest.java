package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/version.test.ts}. */
@DisplayName("version()")
class VersionTest {

  @Test
  @DisplayName("TESTS cases")
  void testsCases() {
    for (TestConstants.Case c : TestConstants.TESTS) {
      try {
        int actualVersion = Version.version(c.value);

        assertTrue(c.expectedValidate, () -> "version(" + c.value + ") should throw");
        assertEquals(c.expectedVersion, Integer.valueOf(actualVersion));
      } catch (RuntimeException e) {
        assertFalse(c.expectedValidate, () -> "version(" + c.value + ") threw unexpectedly");
      }
    }
  }
}
