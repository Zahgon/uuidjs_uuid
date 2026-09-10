package com.github.uuidjs.uuid;

/**
 * Java equivalent of the JavaScript {@code RangeError} thrown by the original
 * TypeScript sources, e.g.
 * {@code throw new RangeError(`UUID byte range ${offset}:${offset + 15} is out of buffer bounds`)}.
 *
 * <p>The TypeScript tests assert {@code assert.throws(..., RangeError)}, so this
 * must remain distinguishable from {@link JSTypeError}.
 */
public class JSRangeError extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JSRangeError(String message) {
    super(message);
  }
}
