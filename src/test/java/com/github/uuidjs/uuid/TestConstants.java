package com.github.uuidjs.uuid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Port of {@code src/test/test_constants.ts}.
 *
 * <p>Table of {@code [uuid value, expected validate(), [expected version()]]}.
 */
final class TestConstants {
  private TestConstants() {}

  /** Mirrors one row of the TS {@code TESTS} array. */
  static final class Case {
    final Object value;
    final boolean expectedValidate;
    final Integer expectedVersion;

    Case(Object value, boolean expectedValidate, Integer expectedVersion) {
      this.value = value;
      this.expectedValidate = expectedValidate;
      this.expectedVersion = expectedVersion;
    }

    Case(Object value, boolean expectedValidate) {
      this(value, expectedValidate, null);
    }
  }

  static final List<Case> TESTS = new ArrayList<>();

  private static void add(Object value, boolean expectedValidate, Integer expectedVersion) {
    TESTS.add(new Case(value, expectedValidate, expectedVersion));
  }

  private static void add(Object value, boolean expectedValidate) {
    TESTS.add(new Case(value, expectedValidate));
  }

  static {
    // constants
    add(Nil.NIL, true, 0);
    add(Max.MAX, true, 15);

    // each version, with either all 0's or all 1's in settable bits
    add("00000000-0000-1000-8000-000000000000", true, 1);
    add("ffffffff-ffff-1fff-8fff-ffffffffffff", true, 1);
    add("00000000-0000-2000-8000-000000000000", true, 2);
    add("ffffffff-ffff-2fff-bfff-ffffffffffff", true, 2);
    add("00000000-0000-3000-8000-000000000000", true, 3);
    add("ffffffff-ffff-3fff-bfff-ffffffffffff", true, 3);
    add("00000000-0000-4000-8000-000000000000", true, 4);
    add("ffffffff-ffff-4fff-bfff-ffffffffffff", true, 4);
    add("00000000-0000-5000-8000-000000000000", true, 5);
    add("ffffffff-ffff-5fff-bfff-ffffffffffff", true, 5);
    add("00000000-0000-6000-8000-000000000000", true, 6);
    add("ffffffff-ffff-6fff-bfff-ffffffffffff", true, 6);
    add("00000000-0000-7000-8000-000000000000", true, 7);
    add("ffffffff-ffff-7fff-bfff-ffffffffffff", true, 7);
    add("00000000-0000-8000-8000-000000000000", true, 8);
    add("ffffffff-ffff-8fff-bfff-ffffffffffff", true, 8);
    add("00000000-0000-9000-8000-000000000000", false);
    add("ffffffff-ffff-9fff-bfff-ffffffffffff", false);
    add("00000000-0000-a000-8000-000000000000", false);
    add("ffffffff-ffff-afff-bfff-ffffffffffff", false);
    add("00000000-0000-b000-8000-000000000000", false);
    add("ffffffff-ffff-bfff-bfff-ffffffffffff", false);
    add("00000000-0000-c000-8000-000000000000", false);
    add("ffffffff-ffff-cfff-bfff-ffffffffffff", false);
    add("00000000-0000-d000-8000-000000000000", false);
    add("ffffffff-ffff-dfff-bfff-ffffffffffff", false);
    add("00000000-0000-e000-8000-000000000000", false);
    add("ffffffff-ffff-efff-bfff-ffffffffffff", false);

    // selection of normal, valid UUIDs
    add("d9428888-122b-11e1-b85c-61cd3cbb3210", true, 1);
    add("000003e8-2363-21ef-b200-325096b39f47", true, 2);
    add("a981a0c2-68b1-35dc-bcfc-296e52ab01ec", true, 3);
    add("109156be-c4fb-41ea-b1b4-efe1671c5836", true, 4);
    add("90123e1c-7512-523e-bb28-76fab9f2f73d", true, 5);
    add("1ef21d2f-1207-6660-8c4f-419efbd44d48", true, 6);
    add("017f22e2-79b0-7cc3-98c4-dc0c0c07398f", true, 7);
    add("0d8f23a0-697f-83ae-802e-48f3756dd581", true, 8);

    // all variant octet values
    add("00000000-0000-1000-0000-000000000000", false);
    add("00000000-0000-1000-1000-000000000000", false);
    add("00000000-0000-1000-2000-000000000000", false);
    add("00000000-0000-1000-3000-000000000000", false);
    add("00000000-0000-1000-4000-000000000000", false);
    add("00000000-0000-1000-5000-000000000000", false);
    add("00000000-0000-1000-6000-000000000000", false);
    add("00000000-0000-1000-7000-000000000000", false);
    add("00000000-0000-1000-8000-000000000000", true, 1);
    add("00000000-0000-1000-9000-000000000000", true, 1);
    add("00000000-0000-1000-a000-000000000000", true, 1);
    add("00000000-0000-1000-b000-000000000000", true, 1);
    add("00000000-0000-1000-c000-000000000000", false);
    add("00000000-0000-1000-d000-000000000000", false);
    add("00000000-0000-1000-e000-000000000000", false);
    add("00000000-0000-1000-f000-000000000000", false);

    // invalid strings
    add("00000000000000000000000000000000", false); // unhyphenated NIL
    add("", false);
    add("invalid uuid string", false);
    add("=Y00a-f*vb*-c-d#-p00f\b-g0h-#i^-j*3&-L00k-\nl---00n-fg000-00p-00r+", false);

    // invalid types
    // NOTE: TS has separate `undefined` and `null` rows; Java collapses both to
    // `null`, so both rows are kept to preserve the case count.
    add(null, false); // undefined
    add(null, false); // null
    add(123, false);
    add(Pattern.compile("regex"), false);
    add(new Date(0), false);
    add(false, false);

    // Add NIL and MAX UUIDs with 1-bit flipped in each position
    for (int charIndex = 0; charIndex < 36; charIndex++) {
      // Skip hyphens and version char
      if (charIndex == 8
          || charIndex == 13
          || charIndex == 14 // version char
          || charIndex == 18
          || charIndex == 23) {
        continue;
      }

      char[] nilChars = Nil.NIL.toCharArray();
      char[] maxChars = Max.MAX.toCharArray();

      for (int i = 0; i < 4; i++) {
        nilChars[charIndex] = Integer.toHexString(0x0 ^ (1 << i)).charAt(0);
        // NIL UUIDs w/ a single 1-bit
        add(new String(nilChars), false);

        // MAX UUIDs w/ a single 0-bit
        maxChars[charIndex] = Integer.toHexString(0xf ^ (1 << i)).charAt(0);
        add(new String(maxChars), false);
      }
    }
  }
}
