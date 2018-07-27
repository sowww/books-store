package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.services.BookService;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
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
import java.io.UnsupportedEncodingException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookStoreApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(BookStoreApplicationTests.class);

    private static Gson gson;
    private static BookRequest bookRequest;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookService bookService;

    @BeforeClass
    public static void setUp() throws Exception {
        gson = new Gson();
        bookRequest = new BookRequest("Name", 1, 150F);
    }

    @Before
    @Transactional
    public void resetBase() {
        bookService.deleteAll();
        bookService.fillBooksRepository();
    }

    @Test
    public void contextLoads() throws Exception {
        MvcResult allBooksResult = mvc.perform(
                get("/api/books")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        String allBooksJson = allBooksResult.getResponse().getContentAsString();
        TypeAdapter<Book[]> booksAdapter = gson.getAdapter(Book[].class);
        Book[] allBooks = booksAdapter.fromJson(allBooksJson);
        for (Book book : allBooks) {
            log.info("!!!!! Book id: {} name: {}", book.getId(), book.getName());
        }
        Assert.assertEquals(allBooks[0].getName(), "Book 1");
    }

    @Test
    public void creatingNewBookFromRequestReturnsIt() throws Exception {
        MvcResult result = mvc.perform(post("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(bookRequest))
        )
                .andExpect(status().isOk())
                .andReturn();
        Book book = mvcResultToBook(result);
        Assert.assertEquals(book.getName(), "Name");
    }

    private Book mvcResultToBook(MvcResult result) throws UnsupportedEncodingException {
        return gson.fromJson(
                result.getResponse().getContentAsString(),
                Book.class);
    }
}
