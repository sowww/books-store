package com.example.spring.bookstore.db.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private User() {
    }

    public User(String name) {
        this();
        setName(name);
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

    public static boolean isUserNameValid(String name) {
        String validationRegex = "[a-zA-Z]+[a-zA-Z0-9_\\s.]*";
        return name.matches(validationRegex);
    }

}
