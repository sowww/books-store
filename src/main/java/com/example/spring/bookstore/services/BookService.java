package com.example.spring.bookstore.services;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.repository.BooksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;

@Service
public class BookService {

    private final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BooksRepository booksRepository;


    public BookService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
        // Filling repo with books
        fillBooksRepository();
    }

    // Fills BooksRepository with dummy books
    private void fillBooksRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int price = 100;
            int count = 1;

            try {
                Book book = new Book(bookName, price, count);
                booksRepository.save(book);
                log.info("Book name: '{}' price: {} count: {}", bookName, price, count);
            } catch (ValidationException e) {
                log.info(e.getMessage());
            }
        }
    }
}