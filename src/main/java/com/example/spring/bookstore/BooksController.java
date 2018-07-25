package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.services.BookService;
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
    private final BookService bookService;

    public BooksController(BookService bookService) {
        // Getting booksRepository
        this.bookService = bookService;
    }

    // Creating a new book with params
    // example: POST /api/books
    @PostMapping(value = "")
    public ResponseEntity<Object> addNewBook(@Valid @RequestBody BookRequest bookRequest, Errors errors) {
        // If there are non-valid props
        if (errors.hasErrors()) {
            // Creating our view from errors
            FieldErrorsView fieldErrorsView = new FieldErrorsView();
            fieldErrorsView.addErrors(errors);
            log.info("POST /api/books errors count: {}", errors.getErrorCount());
            // End response with it
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.BAD_REQUEST);
        }
        // Else add the new book and response with it
        return new ResponseEntity<>(bookService.addBook(bookRequest), HttpStatus.CREATED);
    }

    // Getting all books
    // example: GET /api/books
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllBooks() {
        // Getting books from service and response with it
        return ResponseEntity.ok(bookService.getAll());
    }

    // Getting book by id
    // example: GET /api/books/5
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable Long id) {
        log.info("Getting book by id: {}", id);

        // Getting a book from service
        Optional<Book> book = bookService.getById(id);

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
        try {
            // Trying to delete book
            bookService.deleteById(id);
            // If everything is fine, then response with (204) "No Content"
            return ResponseEntity.noContent().build();
        } catch (BookService.BookNotExistException e) {
            // If we got exception, then response with (404) "Not Found"
            return ResponseEntity.notFound().build();
        }
    }

}
