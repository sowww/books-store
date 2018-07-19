package com.example.spring.bookstore.db.book;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int count;
    private float cost;

    public Book() {}

    public Book(String name, float cost, int count) {
        this.name = name;
        this.cost = cost;
        this.count = count;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public static boolean isNameValid(String name) {
        String validationRegex = "[a-zA-Z]+[\\s+[a-zA-Z0-9,.]*]*";
        return name.matches(validationRegex);
    }
}
