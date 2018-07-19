package com.example.spring.bookstore;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final Logger log = LoggerFactory.getLogger(BooksController.class);
    private final BooksRepository booksRepository;

    public BooksController(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
        fillBookRepository();
    }

    private void fillBookRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int cost = 100;
            int count = 1;

            Book book = new Book(bookName, cost, count);
            booksRepository.save(book);
            log.info("Book name: '{}' cost: {}", bookName, cost);
        }
    }

    @GetMapping("/")
    public Iterable<Book> getAllBooks() {
        Iterable<Book> books = booksRepository.findAll();
        log.info("Get all books: {}", books.spliterator().getExactSizeIfKnown());
        return books;
    }

    @GetMapping(value = "/{id}")
    public Optional<Book> getBookById(@PathVariable Long id) {
        log.info("Get book by id: {}", id);
        return booksRepository.findById(id);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteBookById(@PathVariable Long id) {
        log.info("Delete book by id: {}", id);
        booksRepository.deleteById(id);
    }

    @PostMapping(value = "/{name}/{cost}/{count}")
    public void addNewBook(@PathVariable String name, @PathVariable float cost, @PathVariable int count) {
        log.info("New book: {} {} {}", name, cost, count);
        Book book = new Book(name, cost, count);
        booksRepository.save(book);
    }

}
