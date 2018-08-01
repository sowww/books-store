package com.example.spring.bookstore.service;

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

    /**
     * Creating a new order from order request
     *
     * @param orderRequest request to creating the new order
     * @return Order
     * @throws OrderServiceFieldException
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = OrderServiceFieldException.class
    )
    public Order createOrder(OrderRequest orderRequest) throws OrderServiceFieldException {

        Long userId = orderRequest.getUserId();
        User user;
        if (!userRepository.findById(userId).isPresent()) {
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            log.info("Exception: User doesn't exist");
            throw new OrderServiceFieldException(errorsView);
        } else {
            user = userRepository.findById(userId).get();
        }

        log.info("Creating a new order");
        Order order = new Order();
        Set<OrderItem> orderItems = new HashSet<>();
        float sum = 0;
        // Crating set of ids (to prevent duplicates)
        Set<Long> bookIds = new HashSet<>();

        for (BookItem bookItem : orderRequest.getBooks()) {

            // If bookId is in set already
            if (bookIds.contains(bookItem.getBookId())) {
                // It is duplicate
                FieldErrorsView errorsView = new FieldErrorsView(
                        "books[]:bookId",
                        "Book id is not unique",
                        bookItem.getBookId()
                );
                log.info("Exception: Book id is not unique");
                throw new OrderServiceFieldException(errorsView);
            } else {
                bookIds.add(bookItem.getBookId());
            }

            // Checking if book exists
            if (!bookRepository.findById(bookItem.getBookId()).isPresent()) {
                FieldErrorsView errorsView = new FieldErrorsView(
                        "books[].bookId",
                        "Book doesn't exist",
                        bookItem.getBookId()
                );
                log.info("Exception: Book doesn't exist");
                throw new OrderServiceFieldException(errorsView);
            }

            Book book = bookRepository.findById(bookItem.getBookId()).get();
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
                // If we doesn't have enough books
                FieldErrorsView errorsView = new FieldErrorsView(
                        "quantity",
                        "We doesn't have enough books with id:" + bookItem.getBookId(),
                        booksNeeded
                );
                log.info("Exception: We doesn't have enough books");
                throw new OrderServiceFieldException(errorsView);
            }
            sum += book.getPrice() * bookItem.getQuantity();
            orderItems.add(new OrderItem(book, order, bookItem.getQuantity()));
        }
        // Forming our new order
        order.setTotalPayment(sum);
        order.setStatus(Order.Status.PENDING);
        order.setOrderItems(orderItems);
        order.setUser(user);

        return orderRepository.save(order);
    }

    /**
     * Setting order status to PAID by order id
     *
     * @param id order id
     * @return Changed order
     * @throws OrderServiceFieldException
     */
    public Order orderSetPaidById(Long id) throws OrderServiceFieldException {

        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            if (order.get().getStatus() == Order.Status.PENDING) {
                order.get().setStatus(Order.Status.PAID);
                return orderRepository.save(order.get());
            } else {
                FieldErrorsView fieldErrorsView = new FieldErrorsView(
                        "id",
                        "Order status was already PAID",
                        id
                );
                throw new OrderServiceFieldException(fieldErrorsView);
            }
        } else {
            FieldErrorsView fieldErrorsView = new FieldErrorsView(
                    "id",
                    "Order with this id doesn't exist",
                    id
            );
            throw new OrderServiceFieldException(fieldErrorsView);
        }
    }

    /**
     * Getting all orders
     *
     * @return All orders
     */
    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Getting the order by id
     *
     * @param id order id
     */
    public Optional<Order> getById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Getting orders by userId
     *
     * @param userId user id
     * @throws OrderServiceFieldException
     */
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

    /**
     * Deleting an order by id
     *
     * @param id order id
     * @throws OrderNotExistException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) throws Exception {
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
                    throw new Exception("Book doesn't exist!");
                }
            }
            // After returning the books
            orderRepository.deleteById(id);
        } else {
            throw new OrderNotExistException();
        }
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }

    public static class OrderServiceFieldException extends Exception {
        private FieldErrorsView errorsView;

        public OrderServiceFieldException(FieldErrorsView errorsView) {
            this.errorsView = errorsView;
        }

        public FieldErrorsView getErrorsView() {
            return errorsView;
        }
    }

    public static class OrderNotExistException extends Exception {
        public OrderNotExistException() {
            super("Order doesn't exist");
        }
    }

}
