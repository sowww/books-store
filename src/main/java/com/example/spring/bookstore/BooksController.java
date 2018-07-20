package com.example.spring.bookstore;

import com.example.spring.bookstore.db.book.Book;
import com.example.spring.bookstore.db.book.BooksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    public Iterable<Book> getAllBooks() {
        Iterable<Book> books = booksRepository.findAll();
        log.info("Get all books: {}", books.spliterator().getExactSizeIfKnown());
        return books;
    }

    // Getting book by id
    // example: GET /api/books/5
    @GetMapping(value = "/{id}")
    public Optional<Book> getBookById(@PathVariable Long id,
                                      HttpServletResponse response) {
        log.info("Getting book by id: {}", id);

        // Getting book from repo
        Optional<Book> book = booksRepository.findById(id);

        // If book doesn't exist
        if (!book.isPresent()) {
            log.info("Book with id:{} not found", id);
            // Setting status to 404 and returning nothing
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Book is not found");
            return Optional.empty();
        }

        // else returning book
        return book;
    }

    // Deleting book by id
    // example: DELETE /api/books/5
    @DeleteMapping(value = "/{id}")
    public void deleteBookById(@PathVariable Long id,
                               HttpServletResponse response) {
        log.info("Delete book by id: {}", id);

        // Checking if book exists
        // Getting book from repo
        Optional<Book> book = booksRepository.findById(id);

        // If book doesn't exist
        if (!book.isPresent()) {
            log.info("Book with id:{} not found", id);
            // Setting status to 404
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Book is not found");
        } else {
            log.info("Book with id:{} deleted", id);
            // else deleting the book
            booksRepository.deleteById(id);
            // setting proper response status (or may be I could leave 200)
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.addHeader("message", "Book was deleted");
        }
    }

    // Creating new book with params
    // example: POST /api/books?name=Hello world&price=150.5&count=3
    @PostMapping(value = "")
    public void addNewBook(@RequestParam String name,
                           @RequestParam float price,
                           @RequestParam int count,
                           HttpServletResponse response) {
        log.info("New book: {} {} {}", name, price, count);
        // Checking if name is valid
        if (Book.isNameValid(name)) {
            // then creating a new book
            Book book = new Book(name, price, count);
            // save it to repo
            booksRepository.save(book);
            // setting proper status
            response.setStatus(HttpServletResponse.SC_CREATED);
            // adding response header: message
            response.addHeader("message", "Book was created");
        }
    }

}
