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

    public Order createOrder(OrderRequest orderRequest) throws OrderServiceFieldException {

        Order order = new Order();

        Set<OrderItem> orderItems = new HashSet<>();
        float sum = 0;
        Set<Long> bookIds = new HashSet<>();
        for (BookItem bookItem : orderRequest.getBooks()) {
            if (bookIds.contains(bookItem.getBookId())) {
                FieldErrorsView errorsView = new FieldErrorsView(
                        "books",
                        "Book id is not unique",
                        bookItem.getBookId()
                );
                throw new OrderServiceFieldException(errorsView);
            } else {
                bookIds.add(bookItem.getBookId());
            }
            Book book = bookRepository.findById(bookItem.getBookId()).get();
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

        // Checking if view with this id exists
        if (order.isPresent()) {
            // If view exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited view in repo
                orderRepository.save(order.get());
                return order.get();
            } else {
                // else view was already paid
                FieldErrorsView fieldErrorsView = new FieldErrorsView(
                        "id",
                        "Order status was already PAID",
                        id
                );
                throw new OrderServiceFieldException(fieldErrorsView);
            }
        } else {
            // If view doesn't exists then response with (404) Not Found
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

    public void deleteById(Long id) {
        orderRepository.deleteById(id);
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

}
