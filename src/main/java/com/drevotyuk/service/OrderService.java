package com.drevotyuk.service;

import com.drevotyuk.model.Customer;
import com.drevotyuk.model.Order;
import com.drevotyuk.model.Order.OrderStatus;
import com.drevotyuk.model.Product;
import com.drevotyuk.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {
    @Autowired
    private OrderRepository repository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${customer.url}")
    private String customerServiceUrl;
    @Value("${product.url}")
    private String productServiceUrl;

    public Iterable<Order> getAllOrders() {
        return repository.findAll();
    }

    public ResponseEntity<Order> getOrder(int id) {
        Optional<Order> optOrder = repository.findById(id);
        if (!optOrder.isPresent())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(optOrder.get(), HttpStatus.OK);
    }

    public Iterable<Order> getAllOrdersOfCustomer(int customerId) {
        return repository.findByCustomerId(customerId);
    }

    public ResponseEntity<Order> addOrder(Order order) {
        String customerUrl = String.format("http://%s/" + order.getCustomerId(), customerServiceUrl);
        String productUrl = String.format("http://%s?name=" + order.getProductName(), productServiceUrl);

        // check whether customer and product exists and there's enough quantity of
        // ordered product
        try {
            ResponseEntity<Customer> customerEntity = restTemplate.getForEntity(customerUrl, Customer.class);
            if (!customerEntity.hasBody())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            ResponseEntity<Product> productEntity = restTemplate.getForEntity(productUrl, Product.class);
            if (!productEntity.hasBody())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Product product = productEntity.getBody();
            if (product.getQuantity() < order.getProductQuantity())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            product.setQuantity(product.getQuantity() - order.getProductQuantity());
            restTemplate.put(productUrl, product);

            order.setTotalPrice(product.getPrice() * order.getProductQuantity());
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }

        order.setCreationTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.ORDERED);
        return new ResponseEntity<>(repository.save(order), HttpStatus.CREATED);
    }

    public ResponseEntity<Order> updateOrderById(int id, Order order) {
        Optional<Order> optInitialOrder = repository.findById(id);
        if (!optInitialOrder.isPresent())
            return new ResponseEntity<>(repository.save(order), HttpStatus.CREATED);

        Order initialOrder = optInitialOrder.get();
        String customerUrl = String.format("http://%s/" + order.getCustomerId(), customerServiceUrl);
        String productUrl = String.format("http://%s?name=" + order.getProductName(), productServiceUrl);

        try {
            ResponseEntity<Customer> customerEntity = restTemplate.getForEntity(customerUrl, Customer.class);
            if (!customerEntity.hasBody())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            ResponseEntity<Product> productEntity = restTemplate.getForEntity(productUrl, Product.class);
            if (!productEntity.hasBody())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Product product = productEntity.getBody();
            if (product.getQuantity() + initialOrder.getProductQuantity() < order.getProductQuantity())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            // change quantity of product depending on initial and updated orders
            product.setQuantity(product.getQuantity() + initialOrder.getProductQuantity()
                    - order.getProductQuantity());
            restTemplate.put(productUrl, product);

            initialOrder.setStatus(order.getStatus());
            initialOrder.setCustomerId(order.getCustomerId());
            initialOrder.setProductName(order.getProductName());
            initialOrder.setProductQuantity(order.getProductQuantity());
            initialOrder.setTotalPrice(product.getPrice() * order.getProductQuantity());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(repository.save(initialOrder), HttpStatus.OK);
    }

    public ResponseEntity<Order> deleteOrderById(int id) {
        Optional<Order> optOrder = repository.findById(id);
        if (!optOrder.isPresent())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Order order = optOrder.get();

        String productUrl = String.format(
                "http://%s?name=" + repository.findById(id).get().getProductName(), productServiceUrl);
        try {
            ResponseEntity<Product> productEntity = restTemplate.getForEntity(productUrl, Product.class);
            if (!productEntity.hasBody())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Product product = productEntity.getBody();
            // if order isn't delivered yet, add quantity reserved to order back to product
            if (order.getStatus() == Order.OrderStatus.READY
                    || order.getStatus() == Order.OrderStatus.ORDERED) {
                product.setQuantity(product.getQuantity() + order.getProductQuantity());
                restTemplate.put(productUrl, product);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
