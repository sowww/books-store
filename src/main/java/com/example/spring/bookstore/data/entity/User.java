package com.example.spring.bookstore.data.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {

    private static final String VALIDATION_REGEX = "[a-zA-Z]+[a-zA-Z0-9_\\s.]*";

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
