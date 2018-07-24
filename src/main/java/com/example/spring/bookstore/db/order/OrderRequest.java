package com.example.spring.bookstore.db.order;

import com.example.spring.bookstore.db.book.BookItem;

import java.util.Set;

public class OrderRequest {
    private Set<BookItem> books;
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
