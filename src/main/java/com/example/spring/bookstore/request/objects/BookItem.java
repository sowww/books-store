package com.example.spring.bookstore.request.objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BookItem {
    @NotNull
    private Long bookId;
    @Min(1)
    private int quantity;

    public BookItem() {
    }

    public BookItem(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
