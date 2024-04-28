package com.drevotyuk.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.drevotyuk.model.Order;
import com.drevotyuk.service.OrderService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    @InjectMocks
    private OrderController controller;

    @Mock
    private OrderService service;

    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllOrders() {
        List<Order> orderList = new ArrayList<>();
        orderList.add(new Order(1, "TestProduct", 2));
        orderList.add(new Order(2, "TestProduct2", 1));

        when(service.getAllOrders()).thenReturn(orderList);

        Iterable<Order> orders = controller.getAllOrders();
        Assert.assertNotNull(orders);
        Assert.assertEquals(2, ((List<Order>) orders).size());
    }

    @Test
    public void testGetOrderById() {
        int orderId = 1;
        String productName = "TestProduct";
        Order order = new Order(1, productName, 2);

        when(service.getOrder(orderId)).thenReturn(new ResponseEntity<>(order, HttpStatus.OK));

        ResponseEntity<Order> responseEntity = controller.getOrder(orderId);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(productName, responseEntity.getBody().getProductName());
    }

    @Test
    public void testGetAllOrdersOfCustomer() {
        int customerId = 1;
        List<Order> orderList = new ArrayList<>();
        orderList.add(new Order(customerId, "TestProduct", 2));
        orderList.add(new Order(customerId, "TestProduct2", 1));

        when(service.getAllOrdersOfCustomer(anyInt())).thenReturn(orderList);

        Iterable<Order> orders = controller.getAllOrdersOfCustomer(customerId);
        Assert.assertNotNull(orders);
        Assert.assertEquals(2, ((List<Order>) orders).size());
    }

    @Test
    public void testAddOrder() {
        Order orderToAdd = new Order(1, "TestProduct", 2);
        when(service.addOrder(orderToAdd))
                .thenReturn(new ResponseEntity<>(orderToAdd, HttpStatus.CREATED));

        ResponseEntity<Order> responseEntity = controller.addOrder(orderToAdd);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        Assert.assertEquals(orderToAdd, responseEntity.getBody());
    }

    @Test
    public void testUpdateOrder() {
        int orderId = 1;
        Order orderToUpdate = new Order(1, "UpdatedProduct", 3);

        when(service.updateOrderById(orderId, orderToUpdate))
                .thenReturn(new ResponseEntity<>(orderToUpdate, HttpStatus.OK));

        ResponseEntity<Order> responseEntity = controller.updateOrder(orderId, orderToUpdate);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(orderToUpdate, responseEntity.getBody());
    }

    @Test
    public void testDeleteOrder() {
        int orderId = 1;
        when(service.deleteOrderById(orderId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Order> responseEntity = controller.deleteOrder(orderId);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
