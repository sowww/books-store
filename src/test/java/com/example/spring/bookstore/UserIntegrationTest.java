package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.service.UserService;
import com.example.spring.bookstore.util.DummyFiller;
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

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(BookIntegrationTest.class);
    private final static List<User> dummyUsers = new ArrayList<>();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @BeforeClass
    public static void setUp() {
        DummyFiller.fillDummyUsers(dummyUsers);
    }

    private void clearAndFillUserRepo() {
        userService.deleteAll();
        for (User dummyUser : dummyUsers) {
            User resultUser = userService.addUser(dummyUser);
            log.info("User id: {} name: {} created", resultUser.getId(), resultUser.getName());
        }
    }

    @Before
    public void resetUserRepo() {
        clearAndFillUserRepo();
    }

    @Test
    public void deletingAllUsersWorks() {
        userService.deleteAll();
        Assert.assertEquals(userService.getAll().spliterator().getExactSizeIfKnown(), 0);
    }

    @Test
    public void getAllUsersWillReturnUsers() throws Exception {
        userService.deleteAll();
        userService.addUser(new User("UserName1"));
        userService.addUser(new User("UserName2"));

        mvc.perform(
                get("/api/users").accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("UserName1")))
                .andExpect(jsonPath("$[1].name", is("UserName2")));
    }

    @Test
    public void getUserByIdWorks() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        User firstUser = users.get(0);

        MvcResult bookByIdResult = mvc.perform(
                get("/api/users/" + firstUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        User userFromMvc = mvcResultToClass(bookByIdResult, User.class);
        Assert.assertEquals(userFromMvc.getId(), firstUser.getId());
        Assert.assertEquals(userFromMvc.getName(), firstUser.getName());
    }

    @Test
    public void fillUsersRepoAddsThreeUser() {
        userService.deleteAll();
        userService.fillUsersRepository();
        Assert.assertEquals(
                userService.getAll().spliterator().getExactSizeIfKnown(),
                3
        );
    }
}
