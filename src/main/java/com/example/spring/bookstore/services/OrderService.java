package com.example.spring.bookstore.services;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import com.example.spring.bookstore.db.order.Order;
import com.example.spring.bookstore.db.order.OrderItem;
import com.example.spring.bookstore.db.order.OrdersRepository;
import com.example.spring.bookstore.db.user.User;
import com.example.spring.bookstore.db.user.UsersRepository;
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

    private final OrdersRepository ordersRepository;
    private final BooksRepository booksRepository;
    private final UsersRepository usersRepository;

    public OrderService(OrdersRepository ordersRepository,
                        BooksRepository booksRepository,
                        UsersRepository usersRepository) {
        this.ordersRepository = ordersRepository;
        this.booksRepository = booksRepository;
        this.usersRepository = usersRepository;
    }

    public Order createOrder(OrderRequest orderRequest) throws OrderServiceFieldException {

        Order order = new Order();

        Set<OrderItem> orderItems = new HashSet<>();
        float sum = 0;
        for (BookItem bookItem : orderRequest.getBooks()) {
            Book book = booksRepository.findById(bookItem.getBookId()).get();
            sum += book.getPrice() * bookItem.getQuantity();
            orderItems.add(new OrderItem(book, order, bookItem.getQuantity()));
        }
        Long userId = orderRequest.getUserId();
        if (!usersRepository.findById(userId).isPresent()) {
            FieldErrorsView errorsView = new FieldErrorsView(
                    "userId",
                    "User doesn't exist",
                    userId
            );
            throw new OrderServiceFieldException(errorsView);
        }
        User user = usersRepository.findById(userId).get();
        order.setTotalPayment(sum);
        order.setStatus(Order.Status.PENDING);
        order.setOrderItems(orderItems);
        order.setUser(user);
        ordersRepository.save(order);
        return order;
    }

    public Order orderSetPaidById(Long id) throws OrderServiceFieldException {

        Optional<Order> order = ordersRepository.findById(id);

        // Checking if order with this id exists
        if (order.isPresent()) {
            // If order exists checking if it's not paid already (PENDING)
            if (order.get().getStatus() == Order.Status.PENDING) {
                // If status is PENDING then set it to PAID
                order.get().setStatus(Order.Status.PAID);
                // Save edited order in repo
                ordersRepository.save(order.get());
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
        return ordersRepository.findAll();
    }

    public Optional<Order> getById(Long id) {
        return ordersRepository.findById(id);
    }

    public Iterable<Order> getOrdersByUserId(Long userId) throws OrderServiceFieldException {
        if (usersRepository.findById(userId).isPresent()) {
            return ordersRepository.getOrdersByUserId(userId);
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
        ordersRepository.deleteById(id);
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
