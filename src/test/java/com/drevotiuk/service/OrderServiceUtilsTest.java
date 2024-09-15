package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.drevotiuk.model.Order;
import com.drevotiuk.model.OrderEmailDetails;
import com.drevotiuk.model.OrderItem;
import com.drevotiuk.model.OrderStatus;
import com.drevotiuk.model.OrderView;
import com.drevotiuk.model.exception.InvalidOrderItemException;
import com.drevotiuk.model.exception.OrderNotFoundException;
import com.drevotiuk.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUtilsTest {
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private RabbitTemplate rabbitTemplate;
  private OrderServiceUtils underTest;

  @BeforeEach
  void setUp() {
    underTest = new OrderServiceUtils(orderRepository, rabbitTemplate);
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
    given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

    // when
    Order found = underTest.findById(order.getId());

    // then
    assertThat(found).isEqualTo(order);
    verify(orderRepository).findById(order.getId());
  }

  @Test
  void shouldThrowWhenDidNotFindOrder() {
    // given
    ObjectId orderId = ObjectId.get();
    given(orderRepository.findById(orderId)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> underTest.findById(orderId))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("Order not found");
  }

  @Test
  void shouldFindAllViewsByUserId() {
    // given
    ObjectId userId = ObjectId.get();
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        userId,
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));
    given(orderRepository.findByUserId(userId)).willReturn(Collections.singletonList(order));

    // when
    List<OrderView> foundList = underTest.findAllViewsByUserId(userId);

    // then
    assertThat(foundList).containsOnly(new OrderView(order));
  }

  @Test
  void shouldCalculateTotalPrice() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq(orderItems.get(0)))).willReturn(BigDecimal.TEN);

    // when
    BigDecimal result = underTest.calculateTotalPrice(orderItems);

    // then
    assertThat(result).isEqualTo(BigDecimal.TEN);
  }

  @Test
  void shouldThrowWhenTotalPriceIsNull() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq(orderItems.get(0)))).willReturn(null);

    // when
    // then
    assertThatThrownBy(() -> underTest.calculateTotalPrice(orderItems))
        .isInstanceOf(InvalidOrderItemException.class)
        .hasMessageContaining("Order item is invalid");
  }

  @Test
  void shouldThrowWhenTotalPriceIsOfInvalidType() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    // Receiving invalid type (needed: BigDecimal; got: OrderItem)
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq(orderItems.get(0)))).willReturn(orderItems.get(0));

    // when
    // then
    assertThatThrownBy(() -> underTest.calculateTotalPrice(orderItems))
        .isInstanceOf(InvalidOrderItemException.class)
        .hasMessageContaining("Order item is invalid");
  }

  @Test
  void shouldThrowWhenTotalPriceEqualsZero() {
    // given
    List<OrderItem> orderItems = Collections.singletonList(new OrderItem(ObjectId.get().toString(), 2));
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq(orderItems.get(0)))).willReturn(BigDecimal.ZERO);

    // when
    // then
    assertThatThrownBy(() -> underTest.calculateTotalPrice(orderItems))
        .isInstanceOf(InvalidOrderItemException.class)
        .hasMessageContaining("Order item is invalid");
  }

  @Test
  void shouldSendOrderCreatedEmail() {
    // given
    Order order = new Order(
        ObjectId.get(),
        OrderStatus.ORDERED,
        ObjectId.get(),
        Collections.singletonList(new OrderItem(ObjectId.get().toString(), 5)),
        LocalDateTime.now(),
        BigDecimal.valueOf(145));

    BigDecimal totalPrice = BigDecimal.TEN;

    OrderEmailDetails details = new OrderEmailDetails(
        order.getUserId().toString(),
        LocalDate.now().toString(),
        totalPrice);

    // when
    underTest.sendOrderCreatedEmail(order, totalPrice);

    // then
    verify(rabbitTemplate).convertAndSend(any(), any(), eq(details));
  }
}
