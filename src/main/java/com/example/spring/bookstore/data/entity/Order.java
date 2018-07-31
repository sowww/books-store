package com.example.spring.bookstore.data.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Objects;
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
    private double totalPayment;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<OrderItem> orderItems;

    private Status status;

    public Order() {
    }

    public Order(User user, double totalPayment, Set<OrderItem> orderItems, Status status) {
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

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Double.compare(order.totalPayment, totalPayment) == 0 &&
                Objects.equals(orderId, order.orderId) &&
                Objects.equals(user, order.user) &&
                status == order.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, user, totalPayment, status);
    }

    public enum Status {
        PENDING,
        PAID
    }
}
