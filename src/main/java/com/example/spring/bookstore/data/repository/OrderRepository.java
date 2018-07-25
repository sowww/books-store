package com.example.spring.bookstore.data.repository;

import com.example.spring.bookstore.data.entity.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    Iterable<Order> getOrdersByUserId(Long userId);
}
