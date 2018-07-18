package com.example.spring.bookstore;


import com.example.spring.bookstore.db.order.OrderRepository;
import com.fasterxml.jackson.annotation.JsonRawValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping(value = "/new/{bookIds}/{userId}", produces = "application/json")
    public void createNewOrder(@PathVariable ArrayList<Long> bookIds,
                                       @PathVariable Long userId,
                                       HttpServletResponse response) {
        bookIds.forEach(id -> log.info("Order. Book with id: {}", id));
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        // TODO
    }
}
