package com.drevotiuk.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an order in the system. This class is mapped to the "orders"
 * collection in MongoDB.
 * It contains details about the order, such as its status, the user who placed
 * the order,the items in the order, the total price, and the time the order was
 * placed.
 */
@Document("orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Order {
  /** The unique identifier for the order. */
  @Id
  private ObjectId id;

  /** The status of the order. */
  private OrderStatus status;

  /** The ID of the user who placed the order. */
  private ObjectId userId;

  /** The list of items included in the order. */
  private List<OrderItem> orderItems;

  /** The time when the order was placed. */
  private LocalDateTime orderTime;

  /** The total price of the order. */
  private BigDecimal totalPrice;
}
