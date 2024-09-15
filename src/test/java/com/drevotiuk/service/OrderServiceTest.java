package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.drevotiuk.model.Order;
import com.drevotiuk.model.OrderItem;
import com.drevotiuk.model.OrderStatus;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.model.exception.InvalidOrderItemException;
import com.drevotiuk.model.exception.OrderNotFoundException;
import com.drevotiuk.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private RabbitTemplate rabbitTemplate;
  @Mock
  private OrderServiceUtils orderServiceUtils;
  private OrderService underTest;

  @BeforeEach
  void setUp() {
    underTest = new OrderService(orderRepository, orderServiceUtils, rabbitTemplate);
  }

  @Test
  void shouldFindAllOrdersOfUser() {
    // given
    ObjectId userId = ObjectId.get();

    // when
    underTest.findAll(userId);

    // then
    verify(orderServiceUtils).findAllViewsByUserId(userId);
  }

  @Test
  void shouldFindOrderById() {
    // given
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        ObjectId.get(),
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));
    given(orderServiceUtils.findById(order.getId())).willReturn(order);

    // when
    OrderView found = underTest.find(order.getId(), order.getUserId());

    // then
    assertThat(found).isEqualTo(new OrderView(order));
  }

  @Test
  void shouldThrowWhenDidNotFindOrder() {
    // given
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        ObjectId.get(),
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));
    given(orderServiceUtils.findById(order.getId())).willThrow(new OrderNotFoundException("TEST order not found"));

    // when
    // then
    assertThatThrownBy(() -> underTest.find(order.getId(), order.getUserId()))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("TEST");
  }

  @Test
  void shouldThrowWhenUserIdsDoNotMatch() {
    // given
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        ObjectId.get(),
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));
    given(orderServiceUtils.findById(order.getId())).willReturn(order);

    // when
    // then
    assertThatThrownBy(() -> underTest.find(order.getId(), ObjectId.get())) // Generating unique "userId"
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("UserIDs do not match");
  }

  @Test
  void shouldSaveOrderToDatabase() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    ObjectId userId = ObjectId.get();
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        userId,
        orderItems,
        LocalDateTime.now(),
        BigDecimal.TEN);
    given(orderServiceUtils.calculateTotalPrice(orderItems)).willReturn(BigDecimal.TEN);

    // when
    OrderView saved = underTest.create(orderItems, userId);

    // then
    assertThat(saved).isEqualTo(new OrderView(order));
    verify(orderServiceUtils).sendOrderCreatedEmail(order, BigDecimal.TEN);
    verify(rabbitTemplate).convertAndSend(any(), any(), eq(orderItems));
    verify(orderRepository).save(order);
  }

  @Test
  void shouldThrowWhenOneOrMoreOrderItemsAreInvalid() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    given(orderServiceUtils.calculateTotalPrice(orderItems))
        .willThrow(new InvalidOrderItemException("TEST invalid order"));

    // when
    // then
    assertThatThrownBy(() -> underTest.create(orderItems, ObjectId.get()))
        .isInstanceOf(InvalidOrderItemException.class)
        .hasMessageContaining("TEST");

    verify(orderServiceUtils, never()).sendOrderCreatedEmail(any(), any());
    verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(BigDecimal.class));
    verify(orderRepository, never()).save(any());
  }
}
