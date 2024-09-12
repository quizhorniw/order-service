package com.drevotiuk.repository;

import com.drevotiuk.model.Order;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Order} entities in MongoDB.
 * Includes a custom method to find orders by user ID.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {

  /**
   * Retrieves a list of orders associated with a specific user ID.
   *
   * @param userId the ID of the user whose orders are to be retrieved
   * @return a list of {@link Order} objects that belong to the specified user
   */
  List<Order> findByUserId(ObjectId userId);
}
