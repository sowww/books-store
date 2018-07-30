package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.request.objects.BookItem;
import com.example.spring.bookstore.request.objects.BookRequest;
import com.example.spring.bookstore.request.objects.OrderRequest;
import com.example.spring.bookstore.service.BookService;
import com.example.spring.bookstore.service.OrderService;
import com.example.spring.bookstore.service.UserService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(BookIntegrationTest.class);
    private final static List<Book> dummyBooks = new ArrayList<>();
    private final static List<User> dummyUsers = new ArrayList<>();

    private final static Gson gson = new Gson();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderService orderService;
    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;

    @BeforeClass
    public static void setUp() {
        DummyFiller.fillDummyBooks(dummyBooks);
        DummyFiller.fillDummyUsers(dummyUsers);
    }

    private void clearAndFillBookAndUserRepos() {
        bookService.deleteAll();
        for (Book dummyBook : dummyBooks) {
            BookRequest bookRequest = BookRequest.fromBook(dummyBook);
            bookService.addBook(bookRequest);
        }

        userService.deleteAll();
        for (User dummyUser : dummyUsers) userService.addUser(dummyUser);
    }

    @Before
    public void resetRepos() {
        clearAndFillBookAndUserRepos();
        orderService.deleteAll();
    }

    @Test
    public void createOrderWorks() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();
        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(books.get(0).getId(), 1));
        bookItems.add(new BookItem(books.get(1).getId(), 1));
        OrderRequest orderRequest =
                new OrderRequest(bookItems, users.get(0).getId());
        MvcResult result = mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andReturn();
        OrderView resultOrder = mvcResultToClass(result, OrderView.class);
        log.info("result order: {}", gson.toJson(resultOrder));
        Assert.assertEquals(
                resultOrder.getUserId(),
                users.get(0).getId()
        );
        Set<BookItem> resultBookItems = resultOrder.getBooks();
        for (BookItem resultBookItem : resultBookItems) {
            Long bookId = resultBookItem.getBookId();
            Assert.assertTrue(bookId.equals(books.get(0).getId()) || bookId.equals(books.get(1).getId()));
        }
    }
}
