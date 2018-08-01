package com.example.spring.bookstore.request.objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class OrderRequest {
    @Valid
    @NotNull
    private Set<BookItem> books;
    @NotNull
    private Long userId;

    public OrderRequest() {
    }

    public OrderRequest(Set<BookItem> books, Long userId) {
        this.books = books;
        this.userId = userId;
    }

    public Set<BookItem> getBooks() {
        return books;
    }

    public void setBooks(Set<BookItem> books) {
        this.books = books;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
