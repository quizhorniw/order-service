package com.drevotiuk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.Order;
import com.drevotiuk.model.OrderStatus;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for managing orders for admin users.
 * It includes methods to fetch all orders, find specific orders, and delete
 * orders.
 * Additionally, it interacts with RabbitMQ for messaging related to restoring
 * product quantities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderManagementService {
  @Value("${rabbitmq.exchange.product-service}")
  private String productServiceExchange;
  @Value("${rabbitmq.routingkey.restore-qty}")
  private String restoreQtyRoutingKey;

  private final OrderRepository repository;
  private final OrderServiceUtils serviceUtils;
  private final RabbitTemplate rabbitTemplate;

  /**
   * Retrieves all orders from the repository and maps them to {@link OrderView}
   * objects.
   *
   * @return a {@link List} of {@link OrderView} objects representing all orders
   */
  public List<OrderView> findAll() {
    log.info("Fetching all orders");
    return repository.findAll().stream()
        .map(OrderView::new)
        .collect(Collectors.toList());
  }

  /**
   * Finds a specific order by its ID.
   *
   * @param orderId the ID of the order to retrieve
   * @return an {@link OrderView} object representing the order
   */
  public OrderView find(ObjectId orderId) {
    log.info("Fetching order with ID {}", orderId);
    return new OrderView(serviceUtils.findById(orderId));
  }

  /**
   * Retrieves all orders for a specific user by their user ID.
   *
   * @param userId the ID of the user whose orders are to be retrieved
   * @return a list of {@link OrderView} objects representing the user's orders
   */
  public List<OrderView> findAllOfUser(ObjectId userId) {
    log.info("Fetching all orders of user with ID {}", userId);
    return serviceUtils.findAllViewsByUserId(userId);
  }

  /**
   * Deletes an order by its ID.
   *
   * @param orderId the ID of the order to delete
   * @throws ForbiddenException if the order has already been delivered
   */
  public void delete(ObjectId orderId) {
    log.info("Deleting order with ID {}", orderId);
    Order order = serviceUtils.findById(orderId);
    if (order.getStatus() == OrderStatus.DELIVERED) {
      log.warn("Order has already been delivered and cannot be deleted");
      throw new ForbiddenException("Order has already been delivered and cannot be deleted");
    }
    repository.deleteById(orderId);
    sendRestoreQtyMessage(order);
  }

  /**
   * Sends a message to RabbitMQ to restore the product quantities in the
   * deleted order.
   *
   * @param order the deleted {@link Order}
   */
  private void sendRestoreQtyMessage(Order order) {
    log.info("Sending message to restore qty in order with ID {}", order.getId());
    rabbitTemplate.convertAndSend(productServiceExchange, restoreQtyRoutingKey, order.getOrderItems());
  }
}
