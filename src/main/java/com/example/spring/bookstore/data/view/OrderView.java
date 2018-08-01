package com.example.spring.bookstore.data.view;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.OrderItem;
import com.example.spring.bookstore.request.objects.BookItem;

import java.util.HashSet;
import java.util.Set;

public class OrderView {
    private Long orderId;
    private Long userId;
    private double totalPayment;
    private Set<BookItem> books;
    private Order.Status status;

    private OrderView() {
    }

    public static OrderView fromOrder(Order order) {
        OrderView orderView = new OrderView();
        orderView.orderId = order.getOrderId();
        orderView.userId = order.getUser().getId();
        orderView.totalPayment = order.getTotalPayment();
        Set<BookItem> bookItems = new HashSet<>();
        for (OrderItem orderItem : order.getOrderItems())
            bookItems.add(new BookItem(orderItem.getBook().getId(),
                    orderItem.getQuantity()));
        orderView.books = bookItems;
        orderView.status = order.getStatus();
        return orderView;
    }

    public static Iterable<OrderView> fromOrders(Iterable<Order> orders) {
        HashSet<OrderView> orderViews = new HashSet<>();
        for (Order order : orders) {
            orderViews.add(fromOrder(order));
        }
        return orderViews;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public double getTotalPayment() {
        return totalPayment;
    }

    public Set<BookItem> getBooks() {
        return books;
    }

    public Order.Status getStatus() {
        return status;
    }
}