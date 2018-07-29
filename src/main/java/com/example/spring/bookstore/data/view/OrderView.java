package com.example.spring.bookstore.data.view;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.OrderItem;
import com.example.spring.bookstore.request.objects.BookItem;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class OrderView {
    private Long orderId;
    private Long userId;
    private double totalPayment;
    private Set<BookItem> books;
    private Order.Status status;

    public OrderView() {
    }

    public OrderView(Long orderId, Long userId, double totalPayment, Set<BookItem> books, Order.Status status) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPayment = totalPayment;
        this.books = books;
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

    public static Iterable<OrderView> fromOrders(Iterable<Order> orders) {
        Iterable<OrderView> orderViews = new HashSet<>();
        for (Order order : orders) {
            ((HashSet<OrderView>) orderViews).add(fromOrder(order));
        }
        return orderViews;
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

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderView orderView = (OrderView) o;
        return Double.compare(orderView.totalPayment, totalPayment) == 0 &&
                Objects.equals(orderId, orderView.orderId) &&
                Objects.equals(userId, orderView.userId) &&
                Objects.equals(books, orderView.books) &&
                status == orderView.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userId, totalPayment, books, status);
    }
}