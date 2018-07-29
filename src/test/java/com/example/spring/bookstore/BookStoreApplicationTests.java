package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.service.BookService;
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

import javax.transaction.Transactional;

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookStoreApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(BookStoreApplicationTests.class);

    private static Gson gson;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookService bookService;

    @BeforeClass
    public static void setUp() throws Exception {
        gson = new Gson();

    }

    @Before
    @Transactional
    public void resetBase() {
        bookService.deleteAll();
        bookService.fillBooksRepository();
    }

    @Test
    public void contextLoads() throws Exception {
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
        Assert.assertEquals(allBooks[0].getName(), "Book 1");
        Assert.assertEquals(allBooks[1].getName(), "Book 2");
    }

    @Test
    public void creatingNewBookFromRequestReturnsIt() throws Exception {
        BookRequest bookRequest = new BookRequest("Name", 1, 150D);
        MvcResult result = mvc.perform(
                post("/api/books")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(bookRequest))
        )
                .andExpect(status().isCreated())
                .andReturn();
        Book book = mvcResultToClass(result, Book.class);
        Assert.assertEquals(book.getName(), "Name");
    }


}
