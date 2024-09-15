package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import org.bson.types.ObjectId;

import static org.assertj.core.api.Assertions.*;

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
import com.drevotiuk.model.exception.OrderNotFoundException;
import com.drevotiuk.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderManagementServiceTest {
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private RabbitTemplate rabbitTemplate;
  @Mock
  private OrderServiceUtils orderServiceUtils;
  private OrderManagementService underTest;

  @BeforeEach
  void setUp() {
    underTest = new OrderManagementService(orderRepository, orderServiceUtils, rabbitTemplate);
  }

  @Test
  void shouldFindAllOrders() {
    // when
    underTest.findAll();

    // then
    verify(orderRepository).findAll();
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
    OrderView found = underTest.find(order.getId());

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
    assertThatThrownBy(() -> underTest.find(order.getId()))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("TEST");
  }

  @Test
  void shouldFindAllOrdersOfUser() {
    // given
    ObjectId userId = ObjectId.get();

    // when
    underTest.findAllOfUser(userId);

    // then
    verify(orderServiceUtils).findAllViewsByUserId(userId);
  }

  @Test
  void shouldDeleteOrderById() {
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
    underTest.delete(order.getId());

    // then
    verify(orderRepository).deleteById(order.getId());
    verify(rabbitTemplate).convertAndSend(any(), any(), eq(order.getOrderItems()));
  }

  @Test
  void shouldThrowWhenDidNotFindOrderToDelete() {
    // given
    ObjectId orderId = ObjectId.get();
    given(orderServiceUtils.findById(orderId)).willThrow(new OrderNotFoundException("TEST order not found"));

    // when
    // then
    assertThatThrownBy(() -> underTest.delete(orderId))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("TEST");
  }

  @Test
  void shouldThrowWhenDelitionOfDeliveredOrder() {
    // given
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.DELIVERED,
        ObjectId.get(),
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));
    given(orderServiceUtils.findById(order.getId())).willReturn(order);

    // when
    // then
    assertThatThrownBy(() -> underTest.delete(order.getId()))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Order has already been delivered");

    verify(orderRepository, never()).deleteById(order.getId());
    verify(rabbitTemplate, never()).convertAndSend(any(), any(), eq(order.getOrderItems()));
  }
}
