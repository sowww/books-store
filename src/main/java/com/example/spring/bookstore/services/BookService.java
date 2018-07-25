package com.example.spring.bookstore.services;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.repository.BookRepository;
import com.example.spring.bookstore.request.objects.BookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Optional;

@Service
public class BookService {

    private final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        // Getting repo
        this.bookRepository = bookRepository;
        // Filling repo with books
        fillBooksRepository();
    }

    // Fills BooksRepository with dummy books
    private void fillBooksRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int price = 100;
            int count = 5;

            try {
                Book book = new Book(bookName, price, count);
                bookRepository.save(book);
                log.info("Book name: '{}' price: {} count: {}", bookName, price, count);
            } catch (ValidationException e) {
                log.info(e.getMessage());
            }
        }
    }

    public Book addBook(@Valid BookRequest bookRequest) {
        // Just saving the book from request and return it
        return bookRepository.save(bookRequest.toBook());
    }

    public Iterable<Book> getAll() {
        // Just returning all books from repo
        return bookRepository.findAll();
    }

    public Optional<Book> getById(Long id) {
        // Getting the book by id and returning it
        return bookRepository.findById(id);
    }

    public void deleteById(Long id) throws BookNotExistException {
        // Checking if the book exists
        if (bookRepository.existsById(id)) {
            // If exists then delete it
            bookRepository.deleteById(id);
        } else {
            // Else throw a BookNotExistException
            throw new BookNotExistException();
        }
    }

    public class BookNotExistException extends Exception {
        BookNotExistException() {
            super("Book doesn't exist");
        }
    }
}
