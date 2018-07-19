package com.example.spring.bookstore;


import com.example.spring.bookstore.db.order.BooksOrder;
import com.example.spring.bookstore.db.order.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

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
    public BooksOrder createNewOrder(@RequestParam ArrayList<Long> bookIds,
                                     @RequestParam Long userId,
                                     HttpServletResponse response) {
        log.info("User id from request:{}", userId);

        float sum = 0f;
        for (Long bookId : bookIds) {
            log.info("BooksOrder. Book with id: {}", bookId);
            sum += 100f;
        }
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        BooksOrder booksOrder = new BooksOrder(userId, sum, bookIds, BooksOrder.Status.PENDING);
        ordersRepository.save(booksOrder);

        // TODO
        return booksOrder;
    }

    // JUST FOR TEST
    // REWORK ALL OF THIS!!!

    // Getting all orders by userId
    // example: GET /api/orders/12
    @GetMapping(value = "/{userId}")
    public Iterable<BooksOrder> getBookById(@PathVariable Long userId,
                                            HttpServletResponse response) {
        log.info("Getting orders by userId: {}", userId);

        // TODO
        return ordersRepository.findAll();
    }
}
