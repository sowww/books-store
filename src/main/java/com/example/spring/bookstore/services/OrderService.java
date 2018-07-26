package com.example.spring.bookstore.services;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.OrderItem;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.repository.BookRepository;
import com.example.spring.bookstore.data.repository.OrderRepository;
import com.example.spring.bookstore.data.repository.UserRepository;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.BookItem;
import com.example.spring.bookstore.request.objects.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        BookRepository bookRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // Creating a new order from order request
    @Transactional(propagation = Propagation.REQUIRED)
    public Order createOrder(OrderRequest orderRequest) throws OrderServiceFieldException {

        // Creating new Order object
        Order order = new Order();
        // Creating empty set of OrderItems
        Set<OrderItem> orderItems = new HashSet<>();
        // Creating variable to calculate totalPayment
        float sum = 0;
        // Crating set of ids to track bookIds (to prevent duplicates)
        Set<Long> bookIds = new HashSet<>();

        // Parsing bookItems from order request
        for (BookItem bookItem : orderRequest.getBooks()) {

            // If bookId is in set already
            if (bookIds.contains(bookItem.getBookId())) {
                // It is duplicate
                // Creating errorView
                FieldErrorsView errorsView = new FieldErrorsView(
                        "books",
                        "Book id is not unique",
                        bookItem.getBookId()
                );
                // Throwing it with exception
                throw new OrderServiceFieldException(errorsView);
            } else {
                // If bookIs wasn't in set then add it in set
                bookIds.add(bookItem.getBookId());
            }

            // Getting book from repo by id
            Book book = bookRepository.findById(bookItem.getBookId()).get();
            // Getting number of books from repo
            int booksInStock = book.getQuantity();
            // Getting number of books from request
            int booksNeeded = bookItem.getQuantity();
            // If we have enough books
            if (booksInStock >= booksNeeded) {
                // Setting new book quantity
                book.setQuantity(booksInStock - booksNeeded);
                // Save it to repo
                bookRepository.save(book);
                log.info(
                        "Book {} new quantity: {} - {} = {}",
                        bookItem.getBookId(),
                        booksInStock,
                        booksNeeded,
                        booksInStock - booksNeeded
                );
            } else {
                // If we doesn't have enough books
                // Creating errorView
                FieldErrorsView errorsView = new FieldErrorsView(
                        "quantity",
                        "We doesn't have enough books with id:" + bookItem.getBookId(),
                        booksNeeded
                );
                // Throw it with OrderServiceFieldException
                throw new OrderServiceFieldException(errorsView);
            }
            // Adding books price to sum
            sum += book.getPrice() * bookItem.getQuantity();
            // Adding orderItem to our set
            orderItems.add(new OrderItem(book, order, bookItem.getQuantity()));
        }
        // Getting a user id from request
        Long userId = orderRequest.getUserId();
        // If the user doesn't exist
        if (!userRepository.findById(userId).isPresent()) {
            // Creating errorView
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            // Throw it with OrderServiceFieldException
            throw new OrderServiceFieldException(errorsView);
        }
        // Getting user from repo
        User user = userRepository.findById(userId).get();

        // Forming our new order
        order.setTotalPayment(sum);
        order.setStatus(Order.Status.PENDING);
        order.setOrderItems(orderItems);
        order.setUser(user);
        // Save it to repo and return
        return orderRepository.save(order);
    }

    // Setting order status to PAID by order id
    public Order orderSetPaidById(Long id) throws OrderServiceFieldException {

        // Getting the order
        Optional<Order> order = orderRepository.findById(id);

        // Checking if order with this id exists
        if (order.isPresent()) {
            // If order exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited order in repo and return it
                return orderRepository.save(order.get());
            } else {
                // else order was already paid
                // creating errorView
                FieldErrorsView fieldErrorsView = new FieldErrorsView(
                        "id",
                        "Order status was already PAID",
                        id
                );
                // And throw it with exception
                throw new OrderServiceFieldException(fieldErrorsView);
            }
        } else {
            // If order doesn't exists then creating errorView
            FieldErrorsView fieldErrorsView = new FieldErrorsView(
                    "id",
                    "Order with this id doesn't exist",
                    id
            );
            // And throw it with exception
            throw new OrderServiceFieldException(fieldErrorsView);
        }
    }

    // Getting all orders
    public Iterable<Order> getAllOrders() {
        // Return all orders from repo
        return orderRepository.findAll();
    }

    // Getting the order by id
    public Optional<Order> getById(Long id) {
        // Return the order from repo
        return orderRepository.findById(id);
    }

    // Getting orders by userId
    public Iterable<Order> getOrdersByUserId(Long userId) throws OrderServiceFieldException {
        // Checking if the user exists
        if (userRepository.findById(userId).isPresent()) {
            // If the user exists then return it
            return orderRepository.getOrdersByUserId(userId);
        } else {
            // If it doesn't
            // Creating errorView
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            // And throw it with OrderServiceFieldException
            throw new OrderServiceFieldException(errorsView);
        }
    }

    // Deleting an order
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) throws OrderNotExistException {
        // Checking if order exists
        if (orderRepository.existsById(id)) {
            // Getting the order from repo
            Order order = orderRepository.findById(id).get();
            // Getting the orderItems from the order
            Set<OrderItem> orderItems = order.getOrderItems();
            // For every item in order
            for (OrderItem orderItem : orderItems) {
                // Getting the book id
                Long bookId = orderItem.getBook().getId();
                // Checking if book exists
                if (bookRepository.existsById(bookId)) {
                    // If book exists
                    // Getting it from repo
                    Book book = bookRepository.findById(bookId).get();
                    // Getting number of books in repo
                    int booksIsStock = book.getQuantity();
                    // Adding books from order to our repo
                    int bookNewQuantity = booksIsStock + orderItem.getQuantity();
                    // Set new book quantity
                    book.setQuantity(bookNewQuantity);
                    // And save it to repo
                    bookRepository.save(book);
                    log.info(
                            "Book {} new quantity: {} + {} = {}",
                            bookId,
                            booksIsStock,
                            orderItem.getQuantity(),
                            bookNewQuantity
                    );
                } else {
                    log.info("Book {} doesn't exist", bookId);
                }
            }
            // After returning the books
            // Delete the order
            orderRepository.deleteById(id);
        } else {
            // If order doesn't exist
            // Throw OrderNotExistException
            throw new OrderNotExistException();
        }
    }

    public class OrderServiceFieldException extends Exception {
        private FieldErrorsView errorsView;

        OrderServiceFieldException(FieldErrorsView errorsView) {
            this.errorsView = errorsView;
        }

        public FieldErrorsView getErrorsView() {
            return errorsView;
        }
    }

    public class OrderNotExistException extends Exception {
        OrderNotExistException() {
            super("Order doesn't exist");
        }
    }

}
