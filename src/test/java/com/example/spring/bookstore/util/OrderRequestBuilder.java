package com.example.spring.bookstore.util;

import com.example.spring.bookstore.request.objects.BookItem;
import com.example.spring.bookstore.request.objects.OrderRequest;

import java.util.HashSet;
import java.util.Set;

public class OrderRequestBuilder {
    private Long userId;
    private Set<BookItem> bookItems;

    public OrderRequestBuilder() {
        this.bookItems = new HashSet<>();
    }

    public OrderRequestBuilder setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public OrderRequestBuilder addBook(Long bookId, int quantity) {
        bookItems.add(new BookItem(bookId, quantity));
        return this;
    }

    public OrderRequest build() throws Exception {
        if (bookItems.size() > 0 && userId != null) {
            return new OrderRequest(bookItems, userId);
        } else {
            throw new Exception("Can't create order request");
        }
    }
}
