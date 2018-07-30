package com.example.spring.bookstore.util;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.entity.User;

import java.util.List;

public class DummyFiller {

    public static void fillDummyBooks(List<Book> dummyBooks) {
        dummyBooks.clear();
        dummyBooks.add(new Book("Book 1", 120D, 5));
        dummyBooks.add(new Book("Book 2", 120D, 5));
        dummyBooks.add(new Book("Book 3", 120D, 5));
        dummyBooks.add(new Book("Book 4", 120D, 5));
        dummyBooks.add(new Book("Book 5", 120D, 5));
    }

    public static void fillDummyUsers(List<User> dummyUsers) {
        dummyUsers.clear();
        dummyUsers.add(new User("User 1"));
        dummyUsers.add(new User("User 2"));
        dummyUsers.add(new User("User 3"));
    }

}
