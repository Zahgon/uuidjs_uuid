package com.github.uuidjs.uuid;

/**
 * Java equivalent of a plain JavaScript {@code Error}, as thrown by
 * {@code throw new Error('Random bytes length must be >= 16')} in v1/v4/v7.
 */
public class JSError extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JSError(String message) {
    super(message);
  }
}
