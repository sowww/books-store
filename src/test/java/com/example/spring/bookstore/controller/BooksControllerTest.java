package com.example.spring.bookstore.controller;

import com.example.spring.bookstore.BooksController;
import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.service.BookService;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = BooksController.class, secure = false)
public class BooksControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    private Book book1, book2;
    private Gson gson;

    @Before
    public void prepare() {
        book1 = new Book("Book 1", 130, 10);
        book2 = new Book("Book 2", 100, 5);
        gson = new Gson();
    }

    @Test
    public void getBookReturnBook() throws Exception {
        when(bookService.getById(1L)).thenReturn(ofNullable(book1));
        mvc.perform(get("/api/books/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(book1.getName())))
                .andExpect(jsonPath("$.price", is(book1.getPrice())))
                .andExpect(jsonPath("$.quantity", is(book1.getQuantity())));
    }

    @Test
    public void getBooksReturnBooks() throws Exception {
        List<Book> books = new ArrayList<>();
        books.add(book1);
        books.add(book2);
        when(bookService.getAll()).thenReturn(books);
        mvc.perform(get("/api/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(book1.getName())))
                .andExpect(jsonPath("$[0].price", is(book1.getPrice())))
                .andExpect(jsonPath("$[0].quantity", is(book1.getQuantity())))
                .andExpect(jsonPath("$[1].name", is(book2.getName())))
                .andExpect(jsonPath("$[1].price", is(book2.getPrice())))
                .andExpect(jsonPath("$[1].quantity", is(book2.getQuantity())));
    }

    @Test
    public void bookNotFoundTest() throws Exception {
        mvc.perform(get("/api/books/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContent() throws Exception {
        mvc.perform(delete("/api/books/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteNotExistedBookShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new BookService.BookNotExistException()).when(bookService).deleteById(1L);
        mvc.perform(delete("/api/books/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void createBookReturnIsCreated() throws Exception {
        BookRequest bookRequest = new BookRequest("Book 1", 150.0, 5);
        Book book = new Book("Book 1", 150.0, 5);
        when(bookService.addBook(any())).thenReturn(book);
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(gson.toJson(bookRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Book 1")))
                .andExpect(jsonPath("$.price", is(150.0)))
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    public void creatingBookWithNonValidRequestsReturnBadRequest() throws Exception {
        List<BookRequest> bookRequestList = new ArrayList<>();
        bookRequestList.add(new BookRequest("%&Book 1", 150D, 5));
        bookRequestList.add(new BookRequest("Book 1", 150D, -5));
        bookRequestList.add(new BookRequest("Book 1", -150D, 5));
        for (BookRequest bookRequest : bookRequestList) {
            mvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(bookRequest))
                    .accept(MediaType.APPLICATION_JSON)
            )
                    .andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print());
        }
    }
}
