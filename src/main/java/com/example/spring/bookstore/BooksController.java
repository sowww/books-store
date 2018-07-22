package com.example.spring.bookstore;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import com.example.spring.bookstore.errorhandling.ErrorsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final Logger log = LoggerFactory.getLogger(BooksController.class);
    private final BooksRepository booksRepository;

    public BooksController(BooksRepository booksRepository) {
        // Getting booksRepository
        this.booksRepository = booksRepository;
        // Filling it with books
        fillBooksRepository();
    }

    // Fills BooksRepository with dummy books
    private void fillBooksRepository() {
        for (int i = 1; i <= 10; i++) {

            String bookName = "Book " + i;
            int price = 100;
            int count = 1;

            Book book = new Book(bookName, price, count);
            booksRepository.save(book);
            log.info("Book name: '{}' price: {} count: {}", bookName, price, count);
        }
    }

    // Getting all books
    // example: GET /api/books
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllBooks() {
        // Finding all books
        Iterable<Book> books = booksRepository.findAll();
        log.info("Get all books: {}", books.spliterator().getExactSizeIfKnown());
        return ResponseEntity.ok(books);
    }

    // Getting book by id
    // example: GET /api/books/5
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable Long id) {
        log.info("Getting book by id: {}", id);

        // Getting a book from repo
        Optional<Book> book = booksRepository.findById(id);

        // If the book exists
        if (book.isPresent()) {
            // return the book
            return ResponseEntity.ok(book);
        } else {
            // if the book doesn't exist
            log.info("The book with id:{} is not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Deleting a book by id
    // example: DELETE /api/books/5
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteBookById(@PathVariable Long id) {
        log.info("Delete a book by id: {}", id);

        // Getting the book from repo
        Optional<Book> book = booksRepository.findById(id);

        // Checking if the book exists
        if (book.isPresent()) {
            // If the book exists
            booksRepository.deleteById(id);
            log.info("Book with id:{} deleted", id);
            // Return proper response
            return ResponseEntity.ok().build();
        } else {
            log.info("Book with id:{} not found", id);
            // Return 404
            return ResponseEntity.notFound().build();
        }
    }

    // Creating a new book with params
    // example: POST /api/books?name=Hello world&price=150.5&count=3
    @PostMapping(value = "")
    public ResponseEntity<Object> addNewBook(@RequestParam String name,
                                             @RequestParam float price,
                                             @RequestParam int count) {
        log.info("Creating a new book: {} {} {}", name, price, count);

        // Checking if the name is valid
        if (Book.isNameValid(name)) {
            // If it's valid then creating a new book
            Book book = new Book(name, price, count);
            // save it in the repo
            booksRepository.save(book);
            // setting a proper status
            return new ResponseEntity<>(book, HttpStatus.CREATED);
        } else {
            // If it's not valid, creating errorView with our error
            ErrorsView errorsView = new ErrorsView("name", "Book name is not valid.", name);
            // And Response with this errorView
            return new ResponseEntity<>(errorsView, HttpStatus.BAD_REQUEST);
        }
    }

}
