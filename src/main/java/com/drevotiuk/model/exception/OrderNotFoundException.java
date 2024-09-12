package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates a order was not found.
 * This exception is typically thrown when a requested order does not exist in
 * the system.
 */
public class OrderNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 8089520917657636639L;

  public OrderNotFoundException(String message) {
    super(message);
  }

  public OrderNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public OrderNotFoundException(Throwable cause) {
    super(cause);
  }
}
