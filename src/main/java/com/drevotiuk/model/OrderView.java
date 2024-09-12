package com.drevotiuk.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A view representation of an {@link Order}, containing essential details such
 * as order items, total price, and status. This class is used to present order
 * data in a simplified format.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderView {
  /** The list of items in the order. */
  private List<OrderItem> orderItems;

  /** The total price of the order. */
  private BigDecimal totalPrice;

  /** The status of the order. */
  private OrderStatus status;

  public OrderView(Order order) {
    this.orderItems = order.getOrderItems();
    this.totalPrice = order.getTotalPrice();
    this.status = order.getStatus();
  }
}
