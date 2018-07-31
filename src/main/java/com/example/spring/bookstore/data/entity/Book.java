package com.example.spring.bookstore.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;
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
    private double price;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "book", cascade = CascadeType.PERSIST)
    private Set<OrderItem> orderItems;

    private Book() {
    }

    public Book(String name, double price, int quantity) throws ValidationException {
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

    public double getPrice() {
        return price;
    }

//    public void setPrice(double price) {
//        validatePrice(price);
//        this.price = price;
//    }

    private void validateName(String name) {
        if (!isNameValid(name))
            throw new ValidationException("Book name is not valid");
    }

    private void validatePrice(double price) {
        if (price < 0)
            throw new ValidationException("Book quantity can't be less than 0");
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0)
            throw new ValidationException("Book quantity can't be less than 0");
    }

    private void validateParams(String name, double price, int quantity) throws ValidationException {
        validateName(name);
        validatePrice(price);
        validateQuantity(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return quantity == book.quantity &&
                Double.compare(book.price, price) == 0 &&
                Objects.equals(id, book.id) &&
                Objects.equals(name, book.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, price);
    }
}
