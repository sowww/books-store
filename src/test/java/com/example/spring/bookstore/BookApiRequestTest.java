package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.services.BookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = BooksController.class, secure = false)
public class BookApiRequestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    private Book book1, book2;

    @Before
    public void prepare() {
        book1 = new Book("Book 1", 130, 10);
        book2 = new Book("Book 2", 100, 5);
    }

    @Test
    public void getBookReturnBookTest() throws Exception {
        when(bookService.getById(1L)).thenReturn(java.util.Optional.ofNullable(book1));
        mvc.perform(get("/api/books/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Book 1")))
                .andExpect(jsonPath("$.price", is(130.0)))
                .andExpect(jsonPath("$.quantity", is(10)));
    }

    @Test
    public void getBooksReturnBooksTest() throws Exception {
        Iterable<Book> books = new ArrayList<>();
        ((ArrayList<Book>) books).add(book1);
        ((ArrayList<Book>) books).add(book2);
        when(bookService.getAll()).thenReturn(books);
        mvc.perform(get("/api/books").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Book 1")))
                .andExpect(jsonPath("$[0].price", is(130.0)))
                .andExpect(jsonPath("$[0].quantity", is(10)))
                .andExpect(jsonPath("$[1].name", is("Book 2")))
                .andExpect(jsonPath("$[1].price", is(100.0)))
                .andExpect(jsonPath("$[1].quantity", is(5)));
    }

    @Test
    public void bookNotFoundTest() throws Exception {
        mvc.perform(get("/api/books/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

//    @Test
//    public void createBookReturnBookTest() throws Exception {
//        MockHttpServletRequestBuilder builder =
//                MockMvcRequestBuilders.post("/api/books")
//                        .contentType("application/json")
//                        .requestAttr();
//    }
}
