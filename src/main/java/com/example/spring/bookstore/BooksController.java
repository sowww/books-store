package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.repository.BookRepository;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.BookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final Logger log = LoggerFactory.getLogger(BooksController.class);
    private final BookRepository bookRepository;

    public BooksController(BookRepository bookRepository) {
        // Getting booksRepository
        this.bookRepository = bookRepository;
    }

    // Creating a new book with params
    // example: POST /api/books
    @PostMapping(value = "")
    public ResponseEntity<Object> addNewBook(@Valid @RequestBody BookRequest bookRequest, Errors errors) {

        if (errors.hasErrors()) {
            FieldErrorsView fieldErrorsView = new FieldErrorsView();
            fieldErrorsView.addErrors(errors);
            log.info("POST /api/books errors count: {}", errors.getErrorCount());
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.BAD_REQUEST);
        }

        Book book = bookRequest.toBook();
        bookRepository.save(book);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    // Getting all books
    // example: GET /api/books
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllBooks() {
        // Finding all books
        Iterable<Book> books = bookRepository.findAll();
        log.info("Get all books: {}", books.spliterator().getExactSizeIfKnown());
        return ResponseEntity.ok(books);
    }

    // Getting book by id
    // example: GET /api/books/5
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable Long id) {
        log.info("Getting book by id: {}", id);

        // Getting a book from repo
        Optional<Book> book = bookRepository.findById(id);

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
        Optional<Book> book = bookRepository.findById(id);

        // Checking if the book exists
        if (book.isPresent()) {
            // If the book exists
            bookRepository.deleteById(id);
            log.info("Book with id:{} deleted", id);
            // Return proper response
            return ResponseEntity.ok().build();
        } else {
            log.info("Book with id:{} not found", id);
            // Return 404
            return ResponseEntity.notFound().build();
        }
    }

}
