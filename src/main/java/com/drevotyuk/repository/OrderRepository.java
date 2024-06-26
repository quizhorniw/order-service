package com.drevotyuk.repository;

import com.drevotyuk.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Integer> {
    Iterable<Order> findByCustomerId(int customerId);
}
