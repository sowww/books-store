package com.example.spring.bookstore.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {

    public static final String VALIDATION_REGEX = "[a-zA-Z]+[a-zA-Z0-9_\\s.]*";

    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Set<Order> orders;

    private User() {
    }

    public User(String name) {
        this();
        setName(name);
    }

    public static boolean isUserNameValid(String name) {
        return name.matches(VALIDATION_REGEX);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }
}
