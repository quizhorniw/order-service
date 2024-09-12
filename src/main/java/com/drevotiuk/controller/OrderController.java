package com.drevotiuk.controller;

import com.drevotiuk.model.OrderItem;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.service.OrderService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.Assert.notNull;

/**
 * REST controller for managing user orders.
 * Provides endpoints to retrieve all orders, retrieve a specific order, and
 * create new orders.
 * Each request is tied to a specific user, identified by the user ID in the
 * request header.
 */
@RestController
@RequestMapping("/api/${api.version}/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService service;

  /**
   * Retrieves all orders for the user specified by the {@code userId} in the
   * request header.
   *
   * @param userId the ID of the user whose orders are to be retrieved, passed in
   *               the request header
   * @return a {@link ResponseEntity} containing a list of {@link OrderView}
   *         objects representing the user's orders
   * @throws IllegalArgumentException if {@code userId} is not provided
   */
  @GetMapping
  public ResponseEntity<List<OrderView>> findAll(@RequestHeader("${security.header.id}") ObjectId userId) {
    notNull(userId, "No userID provided");
    List<OrderView> allOrders = service.findAll(userId);
    return ResponseEntity.ok(allOrders);
  }

  /**
   * Retrieves a specific order by its ID, ensuring it belongs to the user
   * specified by the {@code userId}.
   *
   * @param orderId the ID of the order to retrieve
   * @param userId  the ID of the user, passed in the request header, to ensure
   *                the order belongs to the user
   * @return a {@link ResponseEntity} containing the {@link OrderView} of the
   *         specified order
   * @throws IllegalArgumentException if {@code userId} is not provided
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderView> find(@PathVariable ObjectId orderId,
      @RequestHeader("${security.header.id}") ObjectId userId) {
    notNull(userId, "No userID provided");
    OrderView order = service.find(orderId, userId);
    return ResponseEntity.ok(order);
  }

  /**
   * Creates a new order for the user specified by the {@code userId}, using the
   * provided list of {@link OrderItem}s.
   *
   * @param orderItems the list of {@link OrderItem} objects to create the order
   * @param userId     the ID of the user, passed in the request header
   * @return a {@link ResponseEntity} containing the created {@link OrderView}
   *         object
   * @throws IllegalArgumentException if {@code userId} is not provided
   */
  @PostMapping
  public ResponseEntity<OrderView> create(@Valid @RequestBody List<OrderItem> orderItems,
      @RequestHeader("${security.header.id}") ObjectId userId) {
    notNull(userId, "No userID provided");
    OrderView createdOrder = service.create(orderItems, userId);
    return ResponseEntity.ok(createdOrder);
  }
}
