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
    @NotNull(message = "Price can't be null")
    @Min(value = 0, message = "Price can't be less than 0")
    private Double price;
    @NotNull(message = "Quantity can't be null")
    @Min(value = 0, message = "Quantity can't be less than 0")
    private Integer quantity;

    public BookRequest() {
    }

    public BookRequest(String name, Double price, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public static BookRequest fromBook(Book book) {
        return new BookRequest(book.getName(), book.getPrice(), book.getQuantity());
    }

    public Book toBook() {
        return new Book(
                name,
                price,
                quantity
        );
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
