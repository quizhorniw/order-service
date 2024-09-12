package com.drevotiuk.model;

/**
 * Enum representing the various statuses an order can have in the system.
 * Each status indicates a different stage in the order's lifecycle.
 */
public enum OrderStatus {
  /** Indicates that the order has been placed but not yet processed. */
  ORDERED,

  /**
   * Indicates that the order has been shipped and is on its way to the customer.
   */
  SHIPPED,

  /** Indicates that the order has been delivered to the customer. */
  DELIVERED
}
