package com.example.spring.bookstore;


import com.example.spring.bookstore.db.order.Order;
import com.example.spring.bookstore.db.order.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final Logger log = LoggerFactory.getLogger(OrdersController.class);
    private final OrdersRepository ordersRepository;

    public OrdersController(OrdersRepository ordersRepository) {
        // Getting orders repo
        this.ordersRepository = ordersRepository;
    }

    // JUST FOR TEST
    // REWORK ALL OF THIS!!!

    // Creating new order
    // example: POST /api/orders?bookIds=1,2,5,3&userId=12
    @PostMapping(value = "", produces = "application/json")
    public Order createNewOrder(@RequestParam ArrayList<Long> bookIds,
                                @RequestParam Long userId,
                                HttpServletResponse response) {
        log.info("User id from request:{}", userId);

        float sum = 0f;
        for (Long bookId : bookIds) {
            log.info("Order. Book with id: {}", bookId);
            sum += 100f;
        }
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        Order order = new Order(userId, sum, bookIds, Order.Status.PENDING);
        ordersRepository.save(order);

        // TODO
        return order;
    }

    // JUST FOR TEST
    // REWORK ALL OF THIS!!!

    // Getting order by Id
    // example: GET /api/orders/12
    @GetMapping(value = "/{orderId}")
    public Optional<Order> getOrderById(@PathVariable Long orderId,
                                       HttpServletResponse response) {
        log.info("Getting order by id: {}", orderId);

        // TODO
        return ordersRepository.findById(orderId);
    }

    // Getting all orders with userId
    // example: GET /api/orders?userId=12
    @GetMapping(value = "")
    public Iterable<Order> getOrderByUserId(@RequestParam Long userId,
                                            HttpServletResponse response) {
        log.info("Getting orders by userId: {}", userId);

        // TODO
        return null;
    }
}
