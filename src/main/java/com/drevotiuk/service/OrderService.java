package com.drevotiuk.service;

import com.drevotiuk.model.Order;
import com.drevotiuk.model.OrderItem;
import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.model.OrderStatus;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for managing orders within the Order Service.
 * Provides methods to find all orders for a user, retrieve a specific order,
 * and create a new order. It also interacts with RabbitMQ for messaging and
 * uses utility methods from {@link OrderServiceUtils}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
  @Value("${rabbitmq.exchange.product-service}")
  private String productServiceExchange;
  @Value("${rabbitmq.routingkey.fetch-qty}")
  private String fetchQtyRoutingKey;

  private final OrderRepository repository;
  private final OrderServiceUtils serviceUtils;
  private final RabbitTemplate rabbitTemplate;

  /**
   * Retrieves all orders associated with a given user.
   *
   * @param userId the ID of the user whose orders are to be retrieved
   * @return a {@link List} of {@link OrderView} for the specified user
   */
  public List<OrderView> findAll(ObjectId userId) {
    log.info("Fetching all orders of user with ID {}", userId);
    return serviceUtils.findAllViewsByUserId(userId);
  }

  /**
   * Retrieves a specific order and verifies that it belongs to the specified
   * user.
   *
   * @param orderId the ID of the order to be retrieved
   * @param userId  the ID of the user to verify ownership of the order
   * @return the found {@link OrderView} representing user's order
   * @throws ForbiddenException if the order does not belong to the specified
   *                            user
   */
  public OrderView find(ObjectId orderId, ObjectId userId) {
    log.info("Fetching order with ID {}", orderId);
    Order order = serviceUtils.findById(orderId);
    if (!order.getUserId().equals(userId)) {
      log.warn("UserIDs do not match: {}, {}", order.getUserId(), userId);
      throw new ForbiddenException("UserIDs do not match");
    }

    return new OrderView(order);
  }

  /**
   * Creates a new {@link Order} based on the provided order request and user
   * ID.
   * Saves the order to the repository, sends a message to fetch product
   * quantities, and sends an email notification about the order creation.
   *
   * @param orderItems the @{@link List} of {@link OrderItem}
   * @param userId     the ID of the user placing the order
   * @return the found {@link OrderView} representing created order
   */
  public OrderView create(List<OrderItem> orderItems, ObjectId userId) {
    log.info("Adding new order: {}", orderItems.toString());

    BigDecimal totalPrice = serviceUtils.calculateTotalPrice(orderItems);
    Order order = buildOrder(orderItems, userId, totalPrice);
    serviceUtils.sendOrderCreatedEmail(order, totalPrice);
    repository.save(order);

    log.info("Sending message to fetch product quantities; orderID: {}", order.getId());
    rabbitTemplate.convertAndSend(productServiceExchange, fetchQtyRoutingKey, orderItems);

    return new OrderView(order);
  }

  /**
   * Builds an {@link Order} object from the given request, user ID, and
   * current time.
   *
   * @param orderItems the {@link List} of {@link OrderItem}
   * @param userId     the ID of the user placing the order
   * @return the constructed {@link Order} object
   */
  private Order buildOrder(List<OrderItem> orderItems, ObjectId userId, BigDecimal totalPrice) {
    return Order.builder()
        .id(ObjectId.get())
        .status(OrderStatus.ORDERED)
        .userId(userId)
        .orderItems(orderItems)
        .orderTime(LocalDateTime.now())
        .totalPrice(totalPrice)
        .build();
  }
}
