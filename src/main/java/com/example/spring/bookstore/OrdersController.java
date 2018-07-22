package com.example.spring.bookstore;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import com.example.spring.bookstore.db.order.Order;
import com.example.spring.bookstore.db.order.OrdersRepository;
import com.example.spring.bookstore.db.user.UsersRepository;
import com.example.spring.bookstore.errors.FieldErrorsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    // Getting the sum of book prices
    private float getBooksTotalPrice(Set<Long> bookIds) {
        float sum = 0;
        // For every bookId
        for (Long bookId : bookIds) {
            // Adding the book price to sum
            sum += booksRepository.findById(bookId).get().getPrice();
        }
        return sum;
    }

    // Creating a new order
    // example: POST /api/orders?bookIds=1,2,5,3&userId=12
    @PostMapping(value = "", produces = "application/json")
    public ResponseEntity<Object> createNewOrder(@RequestParam HashSet<Long> bookIds,
                                                 @RequestParam Long userId) {

        log.info("Trying to create order. bookIds:{} userId:{}", bookIds, userId);

        // Create a fieldErrorsView
        FieldErrorsView fieldErrorsView = new FieldErrorsView();
        // Checking data for order
        // Setting init value
        boolean isBooksExist = true;
        // For every bookId
        for (Long bookId : bookIds) {
            // Getting the book
            Optional<Book> book = booksRepository.findById(bookId);
            // If the book doesn't exists
            if (!book.isPresent()) {
                // can't create order
                isBooksExist = false;
                // Adding error
                fieldErrorsView.addError(
                        "bookIds",
                        "Book with this id doesn't exist",
                        bookId
                );
            } else {
                // if the book exists checking book count
                if (book.get().getCount() < 1) {
                    // if book count < 1
                    // can't create order
                    isBooksExist = false;
                    // Adding error
                    fieldErrorsView.addError(
                            "bookIds",
                            "This book isn't available (count < 1)",
                            bookId
                    );
                }
            }
        }
        // Checking if user exists
        boolean isUserExists = usersRepository.findById(userId).isPresent();
        if (!isUserExists) fieldErrorsView.addError(
                "userId",
                "User with this id isn't exist",
                userId
        );

        if (isBooksExist && isUserExists) {
            // If data is ok
            log.info("Trying to create order. bookIds:{} userId:{}", bookIds, userId);
            // Getting a books total price
            float totalPrice = getBooksTotalPrice(bookIds);
            // Creating the new order
            Order order = new Order(userId, totalPrice, bookIds, Order.Status.PENDING);
            // Saving it in the repo
            ordersRepository.save(order);
            // Response with (201) Created and returning order
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } else {
            // If something is not ok
            // Response with (404) Not Found and returning fieldErrorsView
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.NOT_FOUND);
        }
    }

    // Getting all orders
    // example: GET /api/orders
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllOrders() {
        // Getting all orders from repo
        Iterable<Order> orders = ordersRepository.findAll();
        log.info("Get all orders: {}", orders.spliterator().getExactSizeIfKnown());
        return ResponseEntity.ok(orders);
    }

    // Getting an order by Id
    // example: GET /api/orders/12
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Getting order by id: {}", id);

        // Getting an order from repo
        Optional<Order> order = ordersRepository.findById(id);

        // If the order exists
        if (order.isPresent()) {
            // returning the order
            return ResponseEntity.ok(order);
        } else {
            // else
            log.info("Order with id:{} not found", id);
            // Returning (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // Getting all orders with userId
    // example: GET /api/orders/filter?userId=12
    @GetMapping(value = "/filter")
    public ResponseEntity<Object> getOrderByUserId(@RequestParam Long userId) {
        log.info("Getting orders by userId: {}", userId);

        // If user exists
        if (usersRepository.findById(userId).isPresent()) {
            // Getting an order from repo
            Iterable<Order> orders = ordersRepository.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } else {
            FieldErrorsView fieldErrorsView = new FieldErrorsView(
                    "userId",
                    "User with this id doesn't exist",
                    userId
            );
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.NOT_FOUND);
        }
    }

    // Setting existing order status to PAID
    // example: PUT /api/orders/12/paid
    @PutMapping(value = "/{id}/paid")
    public ResponseEntity<Object> orderSetPaidById(@PathVariable Long id) {

        Optional<Order> order = ordersRepository.findById(id);

        // Checking if order with this id exists
        if (order.isPresent()) {
            // If order exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited order in repo
                ordersRepository.save(order.get());
                return ResponseEntity.ok(order.get());
            } else {
                // else order was already paid
                FieldErrorsView fieldErrorsView = new FieldErrorsView(
                        "id",
                        "Order status was already PAID",
                        id
                );
                return new ResponseEntity<>(fieldErrorsView, HttpStatus.FORBIDDEN);
            }
        } else {
            // If order doesn't exists then response with (404) Not Found
            FieldErrorsView fieldErrorsView = new FieldErrorsView(
                    "id",
                    "Order with this id doesn't exist",
                    id
            );
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.NOT_FOUND);
        }
    }

    // Deleting order by id
    // example: DELETE /api/orders/12
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteOrderById(@PathVariable Long id) {
        log.info("Deleting order by id: {}", id);
        // Getting an order from the repo
        Optional<Order> order = ordersRepository.findById(id);

        // Checking if order exists
        if (order.isPresent()) {
            // If exists delete it from the repo
            ordersRepository.deleteById(id);
            // and then returning (204) No Content
            return ResponseEntity.noContent().build();
        } else {
            // Else set status to (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
