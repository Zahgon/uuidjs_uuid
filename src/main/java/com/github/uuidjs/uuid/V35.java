package com.github.uuidjs.uuid;

import java.nio.charset.StandardCharsets;

/** Port of {@code src/v35.ts} — the shared engine behind v3 and v5. */
public final class V35 {
  private V35() {}

  public static final String DNS = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
  public static final String URL = "6ba7b811-9dad-11d1-80b4-00c04fd430c8";

  /** Mirrors {@code type HashFunction = (bytes: Uint8Array) => Uint8Array}. */
  @FunctionalInterface
  public interface HashFunction {
    byte[] apply(byte[] bytes);
  }

  /**
   * Mirrors:
   *
   * <pre>
   * export function stringToBytes(str: string) {
   *   str = unescape(encodeURIComponent(str));
   *   const bytes = new Uint8Array(str.length);
   *   for (let i = 0; i &lt; str.length; ++i) {
   *     bytes[i] = str.charCodeAt(i);
   *   }
   *   return bytes;
   * }
   * </pre>
   *
   * <p>The {@code unescape(encodeURIComponent(...))} idiom is the classic pre-
   * {@code TextEncoder} way of getting UTF-8 bytes out of a JS string (the upstream
   * {@code TODO} says as much). {@link String#getBytes(java.nio.charset.Charset)}
   * with UTF-8 is the direct equivalent for well-formed input.
   *
   * <p><b>Unpaired surrogates.</b> {@code encodeURIComponent()} throws
   * {@code URIError: URI malformed} when the string contains a lone surrogate,
   * whereas Java would silently substitute {@code '?'} (U+003F) and produce a
   * <em>different, valid-looking</em> UUID. The surrogate pairing is therefore
   * validated explicitly so the Java port fails exactly where the original does.
   */
  public static byte[] stringToBytes(String str) {
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isHighSurrogate(c)) {
        if (i + 1 >= str.length() || !Character.isLowSurrogate(str.charAt(i + 1))) {
          throw new JSURIError("URI malformed");
        }
        i++; // consume the paired low surrogate
      } else if (Character.isLowSurrogate(c)) {
        // A low surrogate not preceded by a high surrogate is also unpaired.
        throw new JSURIError("URI malformed");
      }
    }
    return str.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Mirrors the default export {@code v35(version, hash, value, namespace, buf, offset)}.
   *
   * <p>{@code value} and {@code namespace} are typed as {@link Object} to mirror the
   * TS union {@code string | Uint8Array} (and {@code UUIDTypes}). The upstream tests
   * deliberately pass {@code undefined}/{@code null} for the namespace and expect a
   * {@code TypeError}, so the looser type is required for behavioural parity.
   *
   * <p>Ordering note: the namespace length check happens <em>before</em> {@code value}
   * is dereferenced, which is why {@code v3('hello')} reports a namespace error
   * rather than a value error.
   */
  static Object v35(
      int version, HashFunction hash, Object value, Object namespace, byte[] buf, Integer offset) {

    byte[] namespaceBytes = (namespace instanceof String) ? Parse.parse(namespace) : (byte[]) namespace;

    if (namespace instanceof String) {
      namespace = Parse.parse(namespace);
    }

    if (namespace == null || ((byte[]) namespace).length != 16) {
      throw new JSTypeError("Namespace must be array-like (16 iterable integer values, 0-255)");
    }

    if (value == null) {
      // JS: `valueBytes.length` on `undefined` throws a TypeError.
      throw new JSTypeError("Cannot read properties of undefined (reading 'length')");
    }
    byte[] valueBytes = (value instanceof String) ? stringToBytes((String) value) : (byte[]) value;

    // Compute hash of namespace and value, Per 4.3
    byte[] bytes = new byte[16 + valueBytes.length];
    System.arraycopy(namespaceBytes, 0, bytes, 0, namespaceBytes.length);
    System.arraycopy(valueBytes, 0, bytes, namespaceBytes.length, valueBytes.length);
    bytes = hash.apply(bytes);

    bytes[6] = (byte) ((bytes[6] & 0x0f) | version);
    bytes[8] = (byte) ((bytes[8] & 0x3f) | 0x80);

    if (buf != null) {
      int off = (offset == null) ? 0 : offset;
      if (off < 0 || off + 16 > buf.length) {
        throw new JSRangeError(
            "UUID byte range " + off + ":" + (off + 15) + " is out of buffer bounds");
      }

      for (int i = 0; i < 16; ++i) {
        buf[off + i] = bytes[i];
      }

      return buf;
    }

    return Stringify.unsafeStringify(bytes, 0);
  }
}
