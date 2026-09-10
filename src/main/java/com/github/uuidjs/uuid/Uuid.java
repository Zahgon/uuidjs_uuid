package com.github.uuidjs.uuid;

/**
 * Port of {@code src/index.ts} — the package's public surface.
 *
 * <pre>
 * export { default as MAX } from './max.js';
 * export { default as NIL } from './nil.js';
 * export { default as parse } from './parse.js';
 * export { default as stringify } from './stringify.js';
 * export type * from './types.js';
 * export { default as v1 } from './v1.js';
 * export { default as v1ToV6 } from './v1ToV6.js';
 * export { default as v3 } from './v3.js';
 * export { default as v4 } from './v4.js';
 * export { default as v5 } from './v5.js';
 * export { default as v6 } from './v6.js';
 * export { default as v6ToV1 } from './v6ToV1.js';
 * export { default as v7 } from './v7.js';
 * export { default as validate } from './validate.js';
 * export { default as version } from './version.js';
 * </pre>
 */
public final class Uuid {
  private Uuid() {}

  public static final String MAX = Max.MAX;
  public static final String NIL = Nil.NIL;

  // --- parse / stringify ---

  public static byte[] parse(Object uuid) {
    return Parse.parse(uuid);
  }

  public static String stringify(byte[] arr) {
    return Stringify.stringify(arr);
  }

  public static String stringify(byte[] arr, int offset) {
    return Stringify.stringify(arr, offset);
  }

  // --- validate / version ---

  public static boolean validate(Object uuid) {
    return Validate.validate(uuid);
  }

  public static int version(Object uuid) {
    return Version.version(uuid);
  }

  // --- v1 ---

  public static String v1() {
    return V1.v1();
  }

  public static String v1(Version1Options options) {
    return V1.v1(options);
  }

  public static byte[] v1(Version1Options options, byte[] buf) {
    return V1.v1(options, buf);
  }

  public static byte[] v1(Version1Options options, byte[] buf, int offset) {
    return V1.v1(options, buf, offset);
  }

  // --- v1 <-> v6 conversion ---

  public static String v1ToV6(String uuid) {
    return V1ToV6.v1ToV6(uuid);
  }

  public static byte[] v1ToV6(byte[] uuid) {
    return V1ToV6.v1ToV6(uuid);
  }

  public static String v6ToV1(String uuid) {
    return V6ToV1.v6ToV1(uuid);
  }

  public static byte[] v6ToV1(byte[] uuid) {
    return V6ToV1.v6ToV1(uuid);
  }

  // --- v3 ---

  public static String v3(String value, String namespace) {
    return V3.v3(value, namespace);
  }

  public static byte[] v3(String value, String namespace, byte[] buf) {
    return V3.v3(value, namespace, buf);
  }

  public static byte[] v3(String value, String namespace, byte[] buf, int offset) {
    return V3.v3(value, namespace, buf, offset);
  }

  // --- v4 ---

  public static String v4() {
    return V4.v4();
  }

  public static String v4(Version4Options options) {
    return V4.v4(options);
  }

  public static byte[] v4(Version4Options options, byte[] buf) {
    return V4.v4(options, buf);
  }

  public static byte[] v4(Version4Options options, byte[] buf, int offset) {
    return V4.v4(options, buf, offset);
  }

  // --- v5 ---

  public static String v5(String value, String namespace) {
    return V5.v5(value, namespace);
  }

  public static byte[] v5(String value, String namespace, byte[] buf) {
    return V5.v5(value, namespace, buf);
  }

  public static byte[] v5(String value, String namespace, byte[] buf, int offset) {
    return V5.v5(value, namespace, buf, offset);
  }

  // --- v6 ---

  public static String v6() {
    return V6.v6();
  }

  public static String v6(Version1Options options) {
    return V6.v6(options);
  }

  public static byte[] v6(Version1Options options, byte[] buf) {
    return V6.v6(options, buf);
  }

  public static byte[] v6(Version1Options options, byte[] buf, int offset) {
    return V6.v6(options, buf, offset);
  }

  // --- v7 ---

  public static String v7() {
    return V7.v7();
  }

  public static String v7(Version7Options options) {
    return V7.v7(options);
  }

  public static byte[] v7(Version7Options options, byte[] buf) {
    return V7.v7(options, buf);
  }

  public static byte[] v7(Version7Options options, byte[] buf, int offset) {
    return V7.v7(options, buf, offset);
  }
}
