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
            // Getting
            int booksInStock = book.getQuantity();
            int booksNeeded = bookItem.getQuantity();
            if (booksInStock >= booksNeeded) {
                book.setQuantity(booksInStock - booksNeeded);
                bookRepository.save(book);
                log.info(
                        "Book {} new quantity: {} - {} = {}",
                        bookItem.getBookId(),
                        booksInStock,
                        booksNeeded,
                        booksInStock - booksNeeded
                );
            } else {
                FieldErrorsView errorsView = new FieldErrorsView(
                        "quantity",
                        "We doesn't have enough books with id:" + bookItem.getBookId(),
                        booksNeeded
                );
                throw new OrderServiceFieldException(errorsView);
            }
            sum += book.getPrice() * bookItem.getQuantity();
            orderItems.add(new OrderItem(book, order, bookItem.getQuantity()));
        }
        Long userId = orderRequest.getUserId();
        if (!userRepository.findById(userId).isPresent()) {
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            throw new OrderServiceFieldException(errorsView);
        }
        User user = userRepository.findById(userId).get();

        order.setTotalPayment(sum);
        order.setStatus(Order.Status.PENDING);
        order.setOrderItems(orderItems);
        order.setUser(user);
        orderRepository.save(order);
        return order;
    }

    public Order orderSetPaidById(Long id) throws OrderServiceFieldException {

        Optional<Order> order = orderRepository.findById(id);

        // Checking if order with this id exists
        if (order.isPresent()) {
            // If order exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited order in repo
                orderRepository.save(order.get());
                return order.get();
            } else {
                // else order was already paid
                FieldErrorsView fieldErrorsView = new FieldErrorsView(
                        "id",
                        "Order status was already PAID",
                        id
                );
                throw new OrderServiceFieldException(fieldErrorsView);
            }
        } else {
            // If order doesn't exists then response with (404) Not Found
            FieldErrorsView fieldErrorsView = new FieldErrorsView(
                    "id",
                    "Order with this id doesn't exist",
                    id
            );
            throw new OrderServiceFieldException(fieldErrorsView);
        }
    }

    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getById(Long id) {
        return orderRepository.findById(id);
    }

    public Iterable<Order> getOrdersByUserId(Long userId) throws OrderServiceFieldException {
        if (userRepository.findById(userId).isPresent()) {
            return orderRepository.getOrdersByUserId(userId);
        } else {
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            throw new OrderServiceFieldException(errorsView);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) throws OrderNotExistException {
        if (orderRepository.existsById(id)) {
            Order order = orderRepository.findById(id).get();
            Set<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                Long bookId = orderItem.getBook().getId();
                if (bookRepository.existsById(bookId)) {
                    Book book = bookRepository.findById(bookId).get();
                    int booksIsStock = book.getQuantity();
                    int bookNewQuantity = booksIsStock + orderItem.getQuantity();
                    book.setQuantity(bookNewQuantity);
                    log.info(
                            "Book {} new quantity: {} + {} = {}",
                            bookId,
                            booksIsStock,
                            orderItem.getQuantity(),
                            bookNewQuantity
                    );
                    bookRepository.save(book);
                } else {
                    log.info("Book {} doesn't exist", bookId);
                }
            }
            orderRepository.deleteById(id);
        } else {
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
