package com.example.spring.bookstore.db.order;

import com.example.spring.bookstore.db.book.BookItem;

import java.util.HashSet;
import java.util.Set;

public class OrderView {
    private Long orderId;
    private Long userId;
    private float totalPayment;
    private Set<BookItem> books;
    private Order.Status status;

    public OrderView() {
    }

    public OrderView(Long orderId, Long userId, float totalPayment, Set<BookItem> books, Order.Status status) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPayment = totalPayment;
        this.books = books;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public float getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(float totalPayment) {
        this.totalPayment = totalPayment;
    }

    public Set<BookItem> getBooks() {
        return books;
    }

    public void setBooks(Set<BookItem> books) {
        this.books = books;
    }

    public Order.Status getStatus() {
        return status;
    }

    public void setStatus(Order.Status status) {
        this.status = status;
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
}