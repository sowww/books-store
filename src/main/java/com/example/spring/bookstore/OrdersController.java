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
        // Getting orders repo
        this.ordersRepository = ordersRepository;
        this.booksRepository = booksRepository;
        this.usersRepository = usersRepository;
    }

    private boolean isOrderCorrect(ArrayList<Long> bookIds, Long userId) {

        for (Long bookId : bookIds) {
            Optional<Book> book = booksRepository.findById(bookId);
            if (!book.isPresent()) {
                return false;
            } else {
                if (book.get().getCount() < 1) {
                    return false;
                }
            }
        }
        if (!usersRepository.findById(userId).isPresent()) return false;

        return true;
    }

    private float getBooksTotalPrice(ArrayList<Long> bookIds) {
        float sum = 0;

        for (Long bookId : bookIds) {
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
        log.info("User id from request:{}", userId);


        if (isOrderCorrect(bookIds, userId)) {
            float totalPrice = getBooksTotalPrice(bookIds);
            Order order = new Order(userId, totalPrice, bookIds, Order.Status.PENDING);
            ordersRepository.save(order);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return order;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    // Getting all orders
    // example: GET /api/orders
    @GetMapping(value = {"", "/"})
    public Iterable<Order> getAllOrders() {
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
}
