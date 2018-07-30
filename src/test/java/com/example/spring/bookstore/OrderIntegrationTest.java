package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Book;
import com.example.spring.bookstore.data.entity.Order;
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
import org.junit.*;
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

import java.util.*;

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
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

    @After
    public void clearOrders() {
        orderService.deleteAll();
    }

    @Test
    public void createOrderWorkingCorrectly() throws Exception {
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

    @Test
    public void creatingOrderWithNoExistentUserOrBookReturnBadRequest() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();
        Long maxUserId = 0L;
        Long maxBookIs = 0L;
        for (User user : users) {
            if (user.getId() > maxUserId) maxUserId = user.getId();
        }
        for (Book book : books) {
            if (book.getId() > maxBookIs) maxBookIs = book.getId();
        }
        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(books.get(0).getId(), 1));
        OrderRequest orderRequestNonValidUserId =
                new OrderRequest(bookItems, maxUserId + 1);
        bookItems.clear();
        bookItems.add(new BookItem(maxBookIs + 1, 1));
        OrderRequest orderRequestNonValidBookId =
                new OrderRequest(bookItems, users.get(0).getId());
        List<OrderRequest> orderRequests =
                Arrays.asList(
                        orderRequestNonValidUserId,
                        orderRequestNonValidBookId
                );
        for (OrderRequest orderRequest : orderRequests) {
            mvc.perform(post("/api/orders")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(gson.toJson(orderRequest))
            )
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    public void cantOrderMoreBooksThanStockQuantity() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        User user = users.get(0);
        List<Book> books = (List<Book>) bookService.getAll();

        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(
                        books.get(0).getId(),
                        books.get(0).getQuantity() + 1
                )
        );
        OrderRequest orderRequest = new OrderRequest(
                bookItems,
                user.getId()
        );

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.fieldErrors[0].rejectedValue",
                        is(books.get(0).getQuantity() + 1)
                ));
    }

    @Test
    public void cantUseNotUniqueBookIdInOrder() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        User user = users.get(0);
        List<Book> books = (List<Book>) bookService.getAll();

        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(books.get(0).getId(), 1));
        bookItems.add(new BookItem(books.get(0).getId(), 2));
        OrderRequest orderRequest = new OrderRequest(
                bookItems,
                user.getId()
        );

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.fieldErrors[0].rejectedValue",
                        is(books.get(0).getId().intValue())
                ));
    }

    @Test
    public void orderCanSetPaid() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(books.get(0).getId(), 1));
        OrderRequest orderRequest = new OrderRequest(
                bookItems,
                users.get(0).getId()
        );
        Order createdOrder = orderService.createOrder(orderRequest);

        mvc.perform(post(
                "/api/orders/" + createdOrder.getOrderId() + "/pay"
        ).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAID")));
    }

    @Test
    public void cantSetPaidIfOrderIsPaid() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(books.get(0).getId(), 1));
        OrderRequest orderRequest = new OrderRequest(
                bookItems,
                users.get(0).getId()
        );
        Order createdOrder = orderService.createOrder(orderRequest);
        orderService.orderSetPaidById(createdOrder.getOrderId());

        mvc.perform(post(
                "/api/orders/" + createdOrder.getOrderId() + "/pay"
        ).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.fieldErrors[0].rejectedValue",
                        is(createdOrder.getOrderId().intValue())
                ));
    }

    @Test
    public void cantSetPaidIfOrderIsNotExist() throws Exception {
        mvc.perform(post(
                "/api/orders/999/pay"
        ).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].rejectedValue", is(999)));
    }
}
