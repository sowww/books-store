package com.example.spring.bookstore.data.entity;

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

    public static final String VALIDATION_REGEX = "[a-zA-Z0-9]+[\\s+[a-zA-Z0-9,.]*]*";

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Name can't be blank")
    @Pattern(regexp = VALIDATION_REGEX, message = "Book name is not valid")
    private String name;

    @NotNull(message = "Quantity can't be null")
    @Min(value = 0, message = "Quantity can't be less than 0")
    private int quantity;

    @NotNull(message = "Price can't be null")
    @Min(value = 0, message = "Price can't be less than 0")
    private float price;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    private Book() {
    }

    public Book(String name, float price, int quantity) throws ValidationException {
        this();
        validateParams(name, price, quantity);
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public static boolean isNameValid(String name) {
        return name.matches(VALIDATION_REGEX);
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
        validateName(name);
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        validatePrice(price);
        this.price = price;
    }

    private void validateName(String name) {
        if (!isNameValid(name))
            throw new ValidationException("Book name is not valid");
    }

    private void validatePrice(float price) {
        if (price < 0)
            throw new ValidationException("Book quantity can't be less than 0");
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0)
            throw new ValidationException("Book quantity can't be less than 0");
    }

    private void validateParams(String name, float price, int quantity) throws ValidationException {
        validateName(name);
        validatePrice(price);
        validateQuantity(quantity);
    }
}
