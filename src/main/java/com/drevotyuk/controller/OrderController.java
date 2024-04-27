package com.drevotyuk.controller;

import com.drevotyuk.model.Order;
import com.drevotyuk.repository.OrderRepository;
import com.drevotyuk.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderRepository repository;
    @Autowired
    private OrderService service;

    @GetMapping
    public Iterable<Order> getAllOrders() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable int id) {
        return service.getOrderById(id);
    }

    @GetMapping(params = "customerId")
    public ResponseEntity<Iterable<Order>> getAllOrdersOfCustomer(@RequestParam int customerId) {
        return service.getAllOrdersByCustomerId(customerId);
    }

    @PostMapping
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        return service.addOrder(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable int id, @RequestBody Order order) {
        return service.updateOrderById(id, order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Order> deleteOrder(@PathVariable int id) {
        return service.deleteOrderById(id);
    }
}
