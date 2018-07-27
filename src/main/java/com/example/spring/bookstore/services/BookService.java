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
        this.bookRepository = bookRepository;
        fillBooksRepository();
    }


    /**
     * Fills BooksRepository with dummy books
     */
    public void fillBooksRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int price = 100;
            int count = 5;

            try {
                Book book = new Book(bookName, price, count);
                bookRepository.save(book);
                log.info("Book id: {} name: '{}' price: {} count: {}", book.getId(), bookName, price, count);
            } catch (ValidationException e) {
                log.info(e.getMessage());
            }
        }
    }

    public Book addBook(@Valid BookRequest bookRequest) {
        return bookRepository.save(bookRequest.toBook());
    }

    public Iterable<Book> getAll() {
        return bookRepository.findAll();
    }

    public void deleteAll() {
        bookRepository.deleteAll();
    }

    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }

    public void deleteById(Long id) throws BookNotExistException {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
        } else {
            throw new BookNotExistException();
        }
    }

    public static class BookNotExistException extends Exception {
        public BookNotExistException() {
            super("Book doesn't exist");
        }
    }
}
