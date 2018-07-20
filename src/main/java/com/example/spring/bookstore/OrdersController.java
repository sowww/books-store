package com.example.spring.bookstore;


import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import com.example.spring.bookstore.db.order.Order;
import com.example.spring.bookstore.db.order.OrdersRepository;
import com.example.spring.bookstore.db.user.UsersRepository;
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
    private final BooksRepository booksRepository;
    private final UsersRepository usersRepository;

    public OrdersController(OrdersRepository ordersRepository,
                            BooksRepository booksRepository,
                            UsersRepository usersRepository) {
        // Getting repos
        this.ordersRepository = ordersRepository;
        this.booksRepository = booksRepository;
        this.usersRepository = usersRepository;
    }

    // Checking if we can create order with this params
    private boolean isOrderCorrect(ArrayList<Long> bookIds, Long userId) {
        // For every book id
        for (Long bookId : bookIds) {
            // Getting the book
            Optional<Book> book = booksRepository.findById(bookId);
            // If the book doesn't exists
            if (!book.isPresent()) {
                // can't create order
                return false;
            } else {
                // if the book exists, checking books count
                if (book.get().getCount() < 1) {
                    // can't create order without book
                    return false;
                }
            }
        }

        // If the user doesn't exists, return false
        if (!usersRepository.findById(userId).isPresent()) return false;

        // In other cases return true
        return true;
    }


    // Getting sum of book prices
    private float getBooksTotalPrice(ArrayList<Long> bookIds) {
        float sum = 0;
        // For every bookId
        for (Long bookId : bookIds) {
            // Adding book price to sum
            sum += booksRepository.findById(bookId).get().getPrice();
        }
        return sum;
    }

    // Creating new order
    // example: POST /api/orders?bookIds=1,2,5,3&userId=12
    @PostMapping(value = "", produces = "application/json")
    public Order createNewOrder(@RequestParam ArrayList<Long> bookIds,
                                @RequestParam Long userId,
                                HttpServletResponse response) {

        log.info("Trying to create order. bookIds:{} userId:{}", bookIds, userId);

        // Checking data for order
        if (isOrderCorrect(bookIds, userId)) {

            // If data is ok
            log.info("Trying to create order. bookIds:{} userId:{}", bookIds, userId);
            // Getting books total price
            float totalPrice = getBooksTotalPrice(bookIds);
            // Creating new order
            Order order = new Order(userId, totalPrice, bookIds, Order.Status.PENDING);
            // Saving it in repo
            ordersRepository.save(order);
            // Response Created (201)
            response.setStatus(HttpServletResponse.SC_CREATED);
            // Returning order
            return order;
        } else {
            // If data is not ok, response with (400)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // And return null
            return null;
        }
    }

    // Getting all orders
    // example: GET /api/orders
    @GetMapping(value = {"", "/"})
    public Iterable<Order> getAllOrders() {
        // Getting all orders from repo
        Iterable<Order> orders = ordersRepository.findAll();
        log.info("Get all orders: {}", orders.spliterator().getExactSizeIfKnown());
        return orders;
    }

    // Getting order by Id
    // example: GET /api/orders/12
    @GetMapping(value = "/{id}")
    public Optional<Order> getOrderById(@PathVariable Long id,
                                        HttpServletResponse response) {
        log.info("Getting order by id: {}", id);

        // Getting order from repo
        Optional<Order> order = ordersRepository.findById(id);

        // If order doesn't exist
        if (!order.isPresent()) {
            log.info("Order with id:{} not found", id);
            // Setting status to 404 and returning nothing
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Order is not found");
            return Optional.empty();
        }

        // else returning order
        return order;
    }

    // Getting all orders with userId
    // example: GET /api/orders/filter?userId=12
    @GetMapping(value = "/filter")
    public Iterable<Order> getOrderByUserId(@RequestParam Long userId,
                                            HttpServletResponse response) {
        log.info("Getting orders by userId: {}", userId);

        // TODO
        return ordersRepository.getOrdersByUserId(userId);
    }

    // Setting existing order status to PAID
    // example: PUT /api/orders/12/paid
    @PutMapping(value = "/{id}/paid")
    public void orderSetPaidById(@PathVariable Long id,
                                 HttpServletResponse response) {
        Optional<Order> order = ordersRepository.findById(id);

        // Checking if order with this id exists
        if (order.isPresent()) {
            // If order exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited order in repo
                ordersRepository.save(order.get());
                response.setStatus(HttpServletResponse.SC_OK);
                response.addHeader("message", "Order status is now PAID");
            } else {
                // else order was already paid
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.addHeader("message", "Order status was already PAID");
            }
        } else {
            // If order doesn't exists then response with 404 status
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Order not found");
        }


        log.info("Getting orders by userId: {}", id);

    }

    // Deleting order by id
    // example: DELETE /api/orders/12
    @DeleteMapping(value = "/{id}")
    public void deleteOrderById(@PathVariable Long id,
                                HttpServletResponse response) {
        log.info("Deleting order by id: {}", id);
        Optional<Order> order = ordersRepository.findById(id);

        // Checking if order exists
        if (order.isPresent()) {
            // If exists then setting status to No Content (204)
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.addHeader("message", "Order is deleted");
            // And delete it from repo
            ordersRepository.deleteById(id);
        } else {
            // Else set status to Not Found (404)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Order is not found");
        }

    }
}
