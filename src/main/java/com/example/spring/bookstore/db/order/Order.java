package com.example.spring.bookstore.db.order;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.HashSet;

@Entity
@Table(name = "Orders")
public class Order {

    public enum Status {
        PENDING,
        PAID
    }

    @Id
    @GeneratedValue
    private Long orderId;
    private Long userId;
    private float totalPayment;
    private HashSet<Long> bookIds;
    private Status status;

    public Order() {
    }

    public Order(Long userId, float totalPayment, HashSet<Long> bookIds, Status status) {
        this.userId = userId;
        this.totalPayment = totalPayment;
        this.bookIds = bookIds;
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

    public HashSet<Long> getBooks() {
        return bookIds;
    }

    public void setBooks(HashSet<Long> books) {
        this.bookIds = books;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
