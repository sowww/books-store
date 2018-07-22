package com.example.spring.bookstore.db.book;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Books")
public class Book {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int count;
    private float price;

    public Book() {}

    public Book(String name, float price, int count) {
        this.name = name;
        this.price = price;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public static boolean isNameValid(String name) {
        String validationRegex = "[a-zA-Z]+[\\s+[a-zA-Z0-9,.]*]*";
        return name.matches(validationRegex);
    }
}
