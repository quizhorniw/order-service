package com.drevotiuk;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.model.exception.InvalidOrderItemException;
import com.drevotiuk.model.exception.OrderNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler that intercepts and handles exceptions globally in
 * the application.
 * Provides specific handling for various exceptions and returns consistent
 * error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  /**
   * Handles validation exceptions such as
   * {@link MethodArgumentNotValidException}.
   * 
   * @param e the {@link MethodArgumentNotValidException} thrown due to
   *          validation failure
   * @return a ResponseEntity containing a map of validation errors with field
   *         names as keys and error messages as values
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleValidationException(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    logException(e);
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * Handles HTTP client errors such as {@link HttpClientErrorException}.
   * 
   * @param e the {@link HttpClientErrorException} thrown for client-related
   *          HTTP errors
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(HttpClientErrorException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles the {@link OrderNotFoundException}.
   * 
   * @param e the {@link OrderNotFoundException} thrown when a product not found
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(OrderNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<Map<String, String>> handleNotFoundException(OrderNotFoundException e) {
    return buildErrorResponse(e, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<Map<String, String>> handleForbiddenException(ForbiddenException e) {
    return buildErrorResponse(e, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles the {@link InvalidOrderItemException}.
   * 
   * @param e the {@link InvalidOrderItemException} thrown when provided order
   *          item is invalid
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(InvalidOrderItemException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleIllegalDetailsException(InvalidOrderItemException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles the {@link IllegalArgumentException}.
   * 
   * @param e the {@link IllegalArgumentException} thrown when an
   *          illegal argument is passed
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles global exceptions such as any uncaught exceptions.
   * 
   * @param e the {@link Exception} thrown when an unexpected error
   *          occurs
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Map<String, String>> handleGlobalException(Exception e) {
    return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Logs the exception details for error tracking and debugging purposes,
   * including the class name and message.
   * 
   * @param e the {@link Exception} that occurred
   */
  private void logException(Exception e) {
    log.error("{} occurred: {}", e.getClass().getSimpleName(), e.getMessage());
  }

  /**
   * Builds a standardized error response for the given exception and HTTP status.
   * 
   * @param e      the {@link Exception} that occurred
   * @param status the HTTP status to return
   * @return a ResponseEntity containing the error message, status, and timestamp
   */
  private ResponseEntity<Map<String, String>> buildErrorResponse(Exception e, HttpStatus status) {
    logException(e);
    Map<String, String> error = new HashMap<>();
    error.put("error", e.getMessage());
    error.put("status", status.toString());
    error.put("timestamp", String.valueOf(System.currentTimeMillis()));
    return ResponseEntity.status(status).body(error);
  }
}
