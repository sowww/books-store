package com.example.spring.bookstore;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        fillBookRepository();
    }

    private void fillBookRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int cost = 100;
            int count = 1;

            Book book = new Book(bookName, cost, count);
            bookRepository.save(book);
            log.info("Book name: '{}' cost: {}", bookName, cost);
        }
    }

    @GetMapping("/all")
    public Iterable<Book> getAllBooks() {
        Iterable<Book> books = bookRepository.findAll();
        log.info("Get all books: {}", books.spliterator().getExactSizeIfKnown());
        return books;
    }

    @GetMapping(value = "/{id}")
    public Optional<Book> getBookById(@PathVariable Long id) {
        log.info("Get book by id: {}", id);
        return bookRepository.findById(id);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteBookById(@PathVariable Long id) {
        log.info("Delete book by id: {}", id);
        bookRepository.deleteById(id);
    }

    @PostMapping(value = "/new/{name}/{cost}/{count}")
    public void addNewBook(@PathVariable String name, @PathVariable float cost, @PathVariable int count) {
        log.info("New book: {} {} {}", name, cost, count);
        Book book = new Book(name, cost, count);
        bookRepository.save(book);
    }

}
