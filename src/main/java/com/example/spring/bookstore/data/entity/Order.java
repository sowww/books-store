package com.example.spring.bookstore.data.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue
    private Long orderId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private float totalPayment;

    @OneToMany(mappedBy = "view", cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    //    private HashSet<Long> bookIds;
    private Status status;

    public Order() {
    }

    public Order(User user, float totalPayment, Set<OrderItem> orderItems, Status status) {
        this.user = user;
        this.totalPayment = totalPayment;
        this.orderItems = orderItems;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public float getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(float totalPayment) {
        this.totalPayment = totalPayment;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        PENDING,
        PAID
    }
}