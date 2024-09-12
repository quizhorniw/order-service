package com.drevotiuk.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.Order;
import com.drevotiuk.model.OrderEmailDetails;
import com.drevotiuk.model.OrderItem;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.model.exception.InvalidOrderItemException;
import com.drevotiuk.model.exception.OrderNotFoundException;
import com.drevotiuk.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling operations related to orders in the Order Service.
 * This class provides methods for finding orders, calculating total prices, and
 * sending notifications
 * about order creation. It also interacts with RabbitMQ for messaging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceUtils {
  @Value("${rabbitmq.exchange.product-service}")
  private String productServiceExchange;
  @Value("${rabbitmq.exchange.notification-service}")
  private String notificationServiceExchange;
  @Value("${rabbitmq.routingkey.total-price}")
  private String totalPriceRoutingKey;
  @Value("${rabbitmq.routingkey.order-created}")
  private String orderCreatedRoutingKey;

  private final OrderRepository repository;
  private final RabbitTemplate rabbitTemplate;

  /**
   * Finds an order by its ID.
   *
   * @param orderId the ID of the order to find
   * @return the found {@link Order}
   * @throws OrderNotFoundException if no order is found with the given ID
   */
  public Order findById(ObjectId orderId) {
    return repository.findById(orderId).orElseThrow(() -> {
      log.warn("Order not found with ID {}", orderId);
      return new OrderNotFoundException("Order not found");
    });
  }

  /**
   * Finds all orders of given user by their ID.
   *
   * @param userId the ID of the order to find
   * @return the {@link List} of {@link Order} objects representing all orders
   * @throws OrderNotFoundException if no order is found with the given ID
   */
  public List<OrderView> findAllViewsByUserId(ObjectId userId) {
    return repository.findByUserId(userId).stream()
        .map(OrderView::new)
        .collect(Collectors.toList());
  }

  /**
   * Calculates the total price of an order based on the order items.
   *
   * @param orderItems the {@link List} of {@link OrderItem} objects
   * @return the total price of the order
   */
  public BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(this::getItemTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Sends an email notification about the creation of an order.
   *
   * @param order      the created {@link Order}
   * @param totalPrice the total price of the order
   */
  public void sendOrderCreatedEmail(Order order, BigDecimal totalPrice) {
    OrderEmailDetails details = buildOrderEmailDetails(order, totalPrice);
    log.info("Sending order created email; orderID: {}", order.getId());
    rabbitTemplate.convertAndSend(notificationServiceExchange, orderCreatedRoutingKey, details);
  }

  /**
   * Calculates the total price for a given order item by sending a message to the
   * product service.
   *
   * @param item the {@link OrderItem}
   * @return the total price of the order item
   * @throws InvalidOrderItemException if the item is invalid or the total price
   *                                   is zero
   */
  private BigDecimal getItemTotalPrice(OrderItem item) {
    log.info("Sending message to calculate total price of product with ID {}", item.getProductId());
    Object message = rabbitTemplate.convertSendAndReceive(productServiceExchange, totalPriceRoutingKey, item);

    BigDecimal totalPrice = validateAndCast(message, BigDecimal.class, item);
    if (BigDecimal.ZERO.equals(totalPrice))
      throw createInvalidOrderItemException(item);

    return totalPrice;
  }

  /**
   * Validates and casts an object to the specified class type.
   *
   * @param obj   the object to validate and cast
   * @param clazz the class type to cast to
   * @param item  the {@link OrderItem} associated with the validation
   * @param <T>   the type of the object
   * @return the cast object
   * @throws InvalidOrderItemException if the object is null or not of the
   *                                   specified class type
   */
  private <T> T validateAndCast(Object obj, Class<T> clazz, OrderItem item) {
    if (obj == null || !clazz.isInstance(obj))
      throw createInvalidOrderItemException(item);

    return clazz.cast(obj);
  }

  /**
   * Builds the details for an order creation email.
   *
   * @param order      the created {@link Order}
   * @param totalPrice the total price of the order
   * @return the {@link OrderEmailDetails}
   */
  private OrderEmailDetails buildOrderEmailDetails(Order order, BigDecimal totalPrice) {
    return OrderEmailDetails.builder()
        .userId(order.getUserId().toString())
        .totalPrice(totalPrice)
        .orderTime(LocalDate.now().toString())
        .build();
  }

  /**
   * Creates an exception indicating that an order item is invalid.
   *
   * @param item the invalid {@link OrderItem}
   * @return the created {@link InvalidOrderItemException}
   */
  private InvalidOrderItemException createInvalidOrderItemException(OrderItem item) {
    log.warn("Order item is invalid: {}", item);
    return new InvalidOrderItemException("Order item is invalid");
  }
}
