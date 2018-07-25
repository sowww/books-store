package com.example.spring.bookstore.request.objects;

import com.example.spring.bookstore.data.entity.Book;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class BookRequest {

    @NotBlank(message = "Name can't be blank")
    @Pattern(regexp = Book.VALIDATION_REGEX, message = "Book name is not valid")
    private String name;
    @NotNull(message = "Quantity can't be null")
    @Min(value = 0, message = "Quantity can't be less than 0")
    private Integer quantity;
    @NotNull(message = "Price can't be null")
    @Min(value = 0, message = "Price can't be less than 0")
    private Float price;

    public BookRequest() {
    }

    public BookRequest(String name, Integer quantity, Float price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Book toBook() {
        return new Book(
                name,
                price != null ? price : 0,
                quantity != null ? quantity : 0
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
}
