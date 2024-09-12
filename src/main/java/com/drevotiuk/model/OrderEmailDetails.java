package com.drevotiuk.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the details required for sending an order confirmation email.
 * It includes the user ID, order time, and total price of the order.
 * This class is typically used when preparing email notifications for order
 * creation.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OrderEmailDetails {
  /** The ID of the user who placed the order. */
  private String userId;

  /** The time when the order was placed. */
  private String orderTime;

  /** The total price of the order. */
  private BigDecimal totalPrice;
}
