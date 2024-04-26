package com.drevotyuk.controller;

import com.drevotyuk.model.Order;
import com.drevotyuk.repository.OrderRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderRepository repository;

    @GetMapping
    public Iterable<Order> getAllOrders() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable int id) {
        Optional<Order> optOrder = repository.findById(id);
        if (!optOrder.isPresent())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(optOrder.get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        // No checking for existing order since customer could order twice
        return new ResponseEntity<>(repository.save(order), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable int id, @RequestBody Order order) {
        Optional<Order> optInitialOrder = repository.findById(id);
        if (!optInitialOrder.isPresent())
            return new ResponseEntity<>(repository.save(order), HttpStatus.CREATED);

        Order initialOrder = optInitialOrder.get();
        initialOrder.setCreationTime(order.getCreationTime());
        initialOrder.setStatus(order.getStatus());

        return new ResponseEntity<>(repository.save(initialOrder), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Order> deleteOrder(@PathVariable int id) {
        if (!repository.existsById(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
