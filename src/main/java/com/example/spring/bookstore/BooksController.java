package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.service.BookService;
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

    /**
     * Creating a new book with params
     * <p>example: POST /api/books</p>
     *
     * @param bookRequest request with books
     * @param errors      errors of validator
     * @return created book response
     */
    @PostMapping(value = "")
    public ResponseEntity<Object> addNewBook(@Valid @RequestBody BookRequest bookRequest, Errors errors) {
        if (errors.hasErrors()) {
            FieldErrorsView fieldErrorsView = new FieldErrorsView();
            fieldErrorsView.addErrors(errors);
            log.info("POST /api/books errors count: {}", errors.getErrorCount());
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(bookService.addBook(bookRequest), HttpStatus.CREATED);
    }


    /**
     * Filling book table with ten dummy books
     * <p>example: POST /api/books/fill</p>
     *
     * @return all books response
     */
    @PostMapping(value = "/fill")
    public ResponseEntity<Object> fillBookRepository() {
        return new ResponseEntity<>(bookService.fillBooksRepository(), HttpStatus.CREATED);
    }

    /**
     * Getting all books
     * <p>example: GET /api/books</p>
     */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllBooks() {
        return ResponseEntity.ok(bookService.getAll());
    }

    /**
     * Getting book by id
     * <p>example: GET /api/books/5</p>
     *
     * @param id book id
     */
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

    /**
     * Deleting a book by id
     * <p>example: DELETE /api/books/5</p>
     *
     * @param id book id
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteBookById(@PathVariable Long id) {
        log.info("Delete a book by id: {}", id);
        try {
            bookService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (BookService.BookNotExistException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
