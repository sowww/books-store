package com.example.spring.bookstore.db.book;

import com.example.spring.bookstore.db.order.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Entity
@Table(name = "Books")
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Name can't be blank")
    @Pattern(regexp = "[a-zA-Z0-9]+[\\s+[a-zA-Z0-9,.]*]*", message = "Book name is not valid")
    private String name;

    @NotNull(message = "Count can't be null")
    @Min(value = 0, message = "Count can't be less than 0")
    private int count;

    @NotNull(message = "Price can't be null")
    @Min(value = 0, message = "Price can't be less than 0")
    private float price;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    private Book() {
    }

    public Book(String name, float price, int count) throws ValidationException {
        this();
        validateParams(name, price, count);
        setName(name);
        setPrice(price);
        setCount(count);
    }

    public static boolean isNameValid(String name) {
        String validationRegex = "[a-zA-Z0-9]+[\\s+[a-zA-Z0-9,.]*]*";
        return (name.matches(validationRegex) && name != null);
    }

    @JsonIgnore
    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
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
//        validateName(name);
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
//        validateCount(count);
        this.count = count;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
//        validatePrice(price);
        this.price = price;
    }

    private void validateParams(String name, float price, int count) throws ValidationException {
        if (!isNameValid(name))
            throw new ValidationException("Book name is not valid");

        if (price < 0)
            throw new ValidationException("Book price can't be less than 0");

        if (count < 0)
            throw new ValidationException("Book count can't be less than 0");
    }
}
