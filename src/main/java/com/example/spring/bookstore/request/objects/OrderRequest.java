package com.example.spring.bookstore.request.objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

public class OrderRequest {
    @Valid
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRequest that = (OrderRequest) o;
        return Objects.equals(books, that.books) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(books, userId);
    }
}
