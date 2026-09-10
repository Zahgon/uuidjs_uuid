package com.github.uuidjs.uuid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Port of {@code src/test/validate.test.ts}. */
@DisplayName("validate()")
class ValidateTest {

  @Test
  @DisplayName("TESTS cases")
  void testsCases() {
    for (TestConstants.Case c : TestConstants.TESTS) {
      assertEquals(
          c.expectedValidate,
          Validate.validate(c.value),
          () -> "validate(" + c.value + ") should be " + c.expectedValidate);
    }
  }
}
