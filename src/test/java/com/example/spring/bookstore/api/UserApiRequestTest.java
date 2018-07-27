package com.example.spring.bookstore.api;

import com.example.spring.bookstore.UsersController;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.services.OrderService;
import com.example.spring.bookstore.services.UserService;
import com.google.gson.Gson;
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
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = UsersController.class, secure = false)
public class UserApiRequestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;
    @MockBean
    private OrderService orderService;

    private Gson gson;
    private User user1, user2;

    @Before
    public void setUp() {
        gson = new Gson();
        user1 = new User("Name 1");
        user2 = new User("Name 2");
    }

    @Test
    public void getAllUsersReturnProperUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        when(userService.getAll()).thenReturn(users);
        mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Name 1")))
                .andExpect(jsonPath("$[1].name", is("Name 2")));
    }

    @Test
    public void getUserByIdReturnUser() throws Exception {
        when(userService.getById(1L)).thenReturn(java.util.Optional.ofNullable(user1));
        mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Name 1")));
    }
}