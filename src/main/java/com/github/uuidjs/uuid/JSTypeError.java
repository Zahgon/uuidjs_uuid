package com.github.uuidjs.uuid;

/**
 * Java equivalent of the JavaScript {@code TypeError} thrown by the original
 * TypeScript sources ({@code throw TypeError('Invalid UUID')}).
 *
 * <p>Modelled as a distinct type (rather than reusing
 * {@link IllegalArgumentException}) so that tests can assert on the exact error
 * class the way the TypeScript tests do.
 */
public class JSTypeError extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JSTypeError(String message) {
    super(message);
  }
}
