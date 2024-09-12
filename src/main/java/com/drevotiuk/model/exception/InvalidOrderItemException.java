package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates that requested order item is invalid.
 * This exception is typically thrown when one or more requested order's fields
 * are invalid by some reason.
 */
public class InvalidOrderItemException extends RuntimeException {
  private static final long serialVersionUID = -9185182146438001338L;

  public InvalidOrderItemException(String message) {
    super(message);
  }

  public InvalidOrderItemException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidOrderItemException(Throwable cause) {
    super(cause);
  }
}
