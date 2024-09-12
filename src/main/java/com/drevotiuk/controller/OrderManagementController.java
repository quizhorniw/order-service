package com.drevotiuk.controller;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.drevotiuk.model.OrderView;
import com.drevotiuk.service.OrderManagementService;

import lombok.RequiredArgsConstructor;

import static org.springframework.util.Assert.isTrue;

/**
 * REST controller for managing orders through administrative actions.
 * Provides endpoints to retrieve all orders, retrieve specific orders,
 * retrieve all orders of a specific user, and delete orders.
 * Access to these endpoints is restricted to users with the "ADMIN" role.
 */
@RestController
@RequestMapping("/api/${api.version}/management/orders")
@RequiredArgsConstructor
public class OrderManagementController {
  private final OrderManagementService managementService;

  /**
   * Retrieves all orders.
   * Access is restricted to users with the "ADMIN" role.
   *
   * @param role the role of the user, retrieved from the request header
   * @return a {@link ResponseEntity} containing a list of {@link OrderView}
   *         objects
   * @throws IllegalArgumentException if {@code role} is not provided or invalid
   */
  @GetMapping
  public ResponseEntity<List<OrderView>> findAll(@RequestHeader("${security.header.role}") String role) {
    isTrue("ADMIN".equals(role), "Access denied");
    List<OrderView> allOrders = managementService.findAll();
    return ResponseEntity.ok(allOrders);
  }

  /**
   * Retrieves a specific order by its ID.
   * Access is restricted to users with the "ADMIN" role.
   *
   * @param orderId the ID of the order to retrieve
   * @param role    the role of the user, retrieved from the request header
   * @return a {@link ResponseEntity} containing the {@link OrderView} of the
   *         specified order
   * @throws IllegalArgumentException if {@code role} is not provided or invalid
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderView> find(@PathVariable ObjectId orderId,
      @RequestHeader("${security.header.role}") String role) {
    isTrue("ADMIN".equals(role), "Access denied");
    OrderView order = managementService.find(orderId);
    return ResponseEntity.ok(order);
  }

  /**
   * Retrieves all orders for a specific user.
   * Access is restricted to users with the "ADMIN" role.
   *
   * @param userId the ID of the user whose orders are to be retrieved
   * @param role   the role of the user, retrieved from the request header
   * @return a {@link ResponseEntity} containing a list of {@link OrderView}
   *         objects for the user
   * @throws IllegalArgumentException if {@code role} is not provided or invalid
   */
  @GetMapping(params = "user")
  public ResponseEntity<List<OrderView>> findAllOfUser(@RequestParam("user") ObjectId userId,
      @RequestHeader("${security.header.role}") String role) {
    isTrue("ADMIN".equals(role), "Access denied");
    List<OrderView> orders = managementService.findAllOfUser(userId);
    return ResponseEntity.ok(orders);
  }

  /**
   * Deletes an order by its ID. If the order has been delivered, a
   * {@link ForbiddenException} is thrown.
   * Access is restricted to users with the "ADMIN" role.
   *
   * @param orderId the ID of the order to delete
   * @param role    the role of the user, retrieved from the request header
   * @return a {@link ResponseEntity} with no content
   * @throws IllegalArgumentException if {@code role} is not provided or invalid
   */
  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> delete(@PathVariable ObjectId orderId,
      @RequestHeader("${security.header.role}") String role) {
    isTrue("ADMIN".equals(role), "Access denied");
    managementService.delete(orderId);
    return ResponseEntity.noContent().build();
  }
}
