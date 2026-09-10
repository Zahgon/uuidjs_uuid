package com.github.uuidjs.uuid;

/**
 * Java equivalent of the JavaScript {@code URIError} raised by
 * {@code encodeURIComponent()} when a string contains an unpaired surrogate.
 *
 * <p>See {@link V35#stringToBytes(String)}.
 */
public class JSURIError extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JSURIError(String message) {
    super(message);
  }
}
