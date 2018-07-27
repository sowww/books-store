package com.example.spring.bookstore.api;


import com.example.spring.bookstore.OrdersController;
import com.example.spring.bookstore.services.BookService;
import com.example.spring.bookstore.services.OrderService;
import com.example.spring.bookstore.services.UserService;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(value = OrdersController.class)
public class OrdersApiRequestTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OrderService orderService;
    @MockBean
    BookService bookService;
    @MockBean
    UserService userService;

    Gson gson;


    @Before
    public void prepare() {
        gson = new Gson();
    }

    @Test
    public void name() {

    }
}
