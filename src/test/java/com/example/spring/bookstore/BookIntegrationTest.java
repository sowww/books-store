package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.service.BookService;
import com.example.spring.bookstore.util.DummyFiller;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(BookIntegrationTest.class);
    private final static List<Book> DUMMY_BOOKS = new ArrayList<>();

    private final static double DOUBLE_DELTA = 0.001D;

    private final static Gson gson = new Gson();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookService bookService;

    @BeforeClass
    public static void setUp() {
        DummyFiller.fillDummyBooks(DUMMY_BOOKS);
    }

    private void clearAndFillBookRepo() {
        log.info("clearAndFillBookRepo()");
        bookService.deleteAll();
        for (Book book : DUMMY_BOOKS) {
            BookRequest bookRequest = BookRequest.fromBook(book);
            Book resultBook = bookService.addBook(bookRequest);
            log.info("User id: {} name: {} created", resultBook.getId(), resultBook.getName());
        }
    }

    @Before
    public void resetBookRepo() {
        clearAndFillBookRepo();
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void applicationContextTest() {
        BookStoreApplication.main(new String[]{});
    }

    @Test
    public void getAllBooksReturnBooks() throws Exception {
        MvcResult allBooksResult = mvc.perform(
                get("/api/books")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        Book[] allBooks = mvcResultToClass(allBooksResult, Book[].class);
        Assert.assertEquals(allBooks[0].getName(), DUMMY_BOOKS.get(0).getName());
        Assert.assertEquals(allBooks[0].getPrice(), DUMMY_BOOKS.get(0).getPrice(), DOUBLE_DELTA);
        Assert.assertEquals(allBooks[0].getQuantity(), DUMMY_BOOKS.get(0).getQuantity());
        Assert.assertEquals(allBooks[1].getName(), DUMMY_BOOKS.get(1).getName());
    }

    @Test
    public void getBookByIdWorks() throws Exception {
        List<Book> books = (List<Book>) bookService.getAll();
        Book firstBook = books.get(0);

        MvcResult bookByIdResult = mvc.perform(
                get("/api/books/" + firstBook.getId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        Book bookFromMvc = mvcResultToClass(bookByIdResult, Book.class);
        Assert.assertEquals(bookFromMvc.getId(), firstBook.getId());
        Assert.assertEquals(bookFromMvc.getName(), firstBook.getName());
        Assert.assertEquals(bookFromMvc.getQuantity(), firstBook.getQuantity());
        Assert.assertEquals(bookFromMvc.getPrice(), firstBook.getPrice(), DOUBLE_DELTA);
    }

    @Test
    public void deleteAllWorks() {
        bookService.deleteAll();
        Assert.assertEquals(
                bookService.getAll().spliterator().getExactSizeIfKnown(),
                0
        );
    }

    @Test
    public void deleteByIdWorks() throws Exception {
        List<Book> books = (List<Book>) bookService.getAll();
        int booksCount = books.size();
        Book firstBook = books.get(0);

        mvc.perform(
                delete("/api/books/" + firstBook.getId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent())
                .andReturn();
        Assert.assertEquals(((List<Book>) bookService.getAll()).size(), booksCount - 1);
        Assert.assertFalse(bookService.getById(firstBook.getId()).isPresent());
    }

    @Test
    public void cantDeleteNonExistentBookBy() throws Exception {
        List<Book> books = (List<Book>) bookService.getAll();
        Long maxBookId = 0L;
        for (Book book : books) {
            if (maxBookId < book.getId()) maxBookId = book.getId();
        }
        int booksCount = books.size();

        mvc.perform(
                delete("/api/books/" + (maxBookId + 1L))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound())
                .andReturn();
        Assert.assertEquals(((List<Book>) bookService.getAll()).size(), booksCount);
    }

    @Test
    public void creatingNewBookFromRequestCreatesIt() throws Exception {
        BookRequest bookRequest = new BookRequest("Name", 150D, 1);
        MvcResult result = mvc.perform(
                post("/api/books")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(bookRequest))
        )
                .andExpect(status().isCreated())
                .andReturn();
        Book bookFromResponse = mvcResultToClass(result, Book.class);
        Long bookId = bookFromResponse.getId();
        Optional<Book> bookFromService = bookService.getById(bookId);
        if (bookFromService.isPresent()) {
            Book book = bookFromService.get();
            Assert.assertEquals(book.getName(), "Name");
            Assert.assertEquals(book.getQuantity(), 1);
            Assert.assertEquals(book.getPrice(), 150D, DOUBLE_DELTA);
        } else {
            throw new Exception("Book is not created");
        }
    }

    @Test
    public void fillBooksRepoAddTenBooks() {
        bookService.deleteAll();
        bookService.fillBooksRepository();
        Assert.assertEquals(
                bookService.getAll().spliterator().getExactSizeIfKnown(),
                10
        );
    }
}
