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
import com.example.spring.bookstore.util.OrderRequestBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.example.spring.bookstore.util.MvcUtils.mvcResultToClass;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(BookIntegrationTest.class);
    private final static List<Book> DUMMY_BOOKS = new ArrayList<>();
    private final static List<User> DUMMY_USERS = new ArrayList<>();

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
        DummyFiller.fillDummyBooks(DUMMY_BOOKS);
        DummyFiller.fillDummyUsers(DUMMY_USERS);
    }

    private void clearAndFillBookAndUserRepos() {
        bookService.deleteAll();
        for (Book dummyBook : DUMMY_BOOKS) {
            BookRequest bookRequest = BookRequest.fromBook(dummyBook);
            bookService.addBook(bookRequest);
        }

        userService.deleteAll();
        for (User dummyUser : DUMMY_USERS) userService.addUser(dummyUser);
    }

    @Before
    public void resetRepos() {
        orderService.deleteAll();
        clearAndFillBookAndUserRepos();
    }

    @After
    public void clearOrders() {
        orderService.deleteAll();
    }

    @Test
    public void createOrderWorkingCorrectly() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .addBook(books.get(1).getId(), 1)
                .build();

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
        for (User user : users) if (user.getId() > maxUserId) maxUserId = user.getId();
        Long maxBookIs = 0L;
        for (Book book : books) if (book.getId() > maxBookIs) maxBookIs = book.getId();

        OrderRequest orderRequestNonValidUserId = new OrderRequestBuilder()
                .setUserId(maxUserId + 1)
                .addBook(books.get(0).getId(), 1)
                .build();

        OrderRequest orderRequestNonValidBookId = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(maxBookIs + 1, 1)
                .build();

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
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), books.get(0).getQuantity() + 1)
                .build();

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
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .addBook(books.get(0).getId(), 2)
                .build();

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
    public void BooksTransactionRollingBackOnOrderCreateException() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();
        int book0Quantity = books.get(0).getQuantity();
        int book1Quantity = books.get(1).getQuantity();
        int book2Quantity = books.get(2).getQuantity();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .addBook(books.get(1).getId(), 1)
                .addBook(books.get(2).getId(), 1)
                .addBook(books.get(0).getId(), 1)
                .build();

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest());

        books = (List<Book>) bookService.getAll();
        Assert.assertEquals(books.get(0).getQuantity(), book0Quantity);
        Assert.assertEquals(books.get(1).getQuantity(), book1Quantity);
        Assert.assertEquals(books.get(2).getQuantity(), book2Quantity);
    }

    @Test
    public void orderCanSetPaid() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

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

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

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

    @Test
    public void getAllOrdersReturnAllOrderViews() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest1 = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

        OrderRequest orderRequest2 = new OrderRequestBuilder()
                .setUserId(users.get(1).getId())
                .addBook(books.get(1).getId(), 2)
                .build();
        List<OrderRequest> orderRequests = Arrays.asList(orderRequest1, orderRequest2);

        for (OrderRequest orderRequest : orderRequests) {
            mvc.perform(post("/api/orders")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(gson.toJson(orderRequest))
            )
                    .andExpect(status().isCreated());
        }

        MvcResult result = mvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        OrderView[] orderViews = mvcResultToClass(result, OrderView[].class);
        Assert.assertEquals(orderViews.length, 2);
        for (OrderView orderView : orderViews) {
            if (orderView.getUserId().equals(users.get(0).getId())) {
                Set<BookItem> bookItems = orderView.getBooks();
                Assert.assertEquals(bookItems.spliterator().getExactSizeIfKnown(), 1);
                BookItem bookItem = bookItems.stream().findFirst().get();
                Assert.assertEquals(bookItem.getBookId(), books.get(0).getId());
                Assert.assertEquals(bookItem.getQuantity(), 1);
            } else if (orderView.getUserId().equals(users.get(1).getId())) {
                Set<BookItem> bookItems = orderView.getBooks();
                Assert.assertEquals(bookItems.spliterator().getExactSizeIfKnown(), 1);
                BookItem bookItem = bookItems.stream().findFirst().get();
                Assert.assertEquals(bookItem.getBookId(), books.get(1).getId());
                Assert.assertEquals(bookItem.getQuantity(), 2);
            } else {
                throw new Exception("UserId was not in order requests");
            }
        }
    }

    @Test
    public void getByIdReturnProperOrder() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

        MvcResult resultAfterCreate = mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(users.get(0).getId().intValue())))
                .andExpect(jsonPath("$.books[0].bookId", is(books.get(0).getId().intValue())))
                .andExpect(jsonPath("$.books[0].quantity", is(1)))
                .andReturn();

        OrderView orderViewAfterCreate = mvcResultToClass(resultAfterCreate, OrderView.class);
        Long orderId = orderViewAfterCreate.getOrderId();

        mvc.perform(get("/api/orders/" + orderId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(users.get(0).getId().intValue())))
                .andExpect(jsonPath("$.books[0].bookId", is(books.get(0).getId().intValue())))
                .andExpect(jsonPath("$.books[0].quantity", is(1)));
    }

    @Test
    public void getOrdersByUserIdWorks() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest1 = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

        OrderRequest orderRequest2 = new OrderRequestBuilder()
                .setUserId(users.get(1).getId())
                .addBook(books.get(1).getId(), 2)
                .build();
        List<OrderRequest> orderRequests = Arrays.asList(orderRequest1, orderRequest2);

        for (OrderRequest orderRequest : orderRequests) {
            mvc.perform(post("/api/orders")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(gson.toJson(orderRequest))
            )
                    .andExpect(status().isCreated());
        }

        mvc.perform(get("/api/orders/filter?userId=" + users.get(1).getId())
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(users.get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].books[0].bookId", is(books.get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].books[0].quantity", is(2)));
    }

    @Test
    public void cantGetOrdersFromNonExistentUserId() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        Long maxUserId = 0L;
        for (User user : users) if (user.getId() > maxUserId) maxUserId = user.getId();

        mvc.perform(get("/api/orders/filter?userId=" + (maxUserId + 1))
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteOrderByIdIsWorking() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest1 = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();

        OrderRequest orderRequest2 = new OrderRequestBuilder()
                .setUserId(users.get(1).getId())
                .addBook(books.get(1).getId(), 2)
                .build();

        MvcResult result1 = mvc.perform(
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(gson.toJson(orderRequest1))
        )
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mvc.perform(
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(gson.toJson(orderRequest2))
        )
                .andExpect(status().isCreated())
                .andReturn();

        OrderView orderViewFromRequest1 = mvcResultToClass(result1, OrderView.class);
        OrderView orderViewFromRequest2 = mvcResultToClass(result2, OrderView.class);

        mvc.perform(delete("/api/orders/" + orderViewFromRequest2.getOrderId())
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].orderId",
                        is(orderViewFromRequest1.getOrderId().intValue())
                ))
                .andExpect(jsonPath(
                        "$[0].books[0].bookId",
                        is(books.get(0).getId().intValue())
                ));
    }

    @Test
    public void cantDeleteOrderWithNonExistentId() throws Exception {
        List<User> users = (List<User>) userService.getAll();
        List<Book> books = (List<Book>) bookService.getAll();

        OrderRequest orderRequest = new OrderRequestBuilder()
                .setUserId(users.get(0).getId())
                .addBook(books.get(0).getId(), 1)
                .build();


        MvcResult result = mvc.perform(
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isCreated())
                .andReturn();

        OrderView orderView = mvcResultToClass(result, OrderView.class);

        mvc.perform(delete("/api/orders/" + orderView.getOrderId() + 1)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());

        // Checking if our created order wasn't deleted
        mvc.perform(get("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].orderId",
                        is(orderView.getOrderId().intValue())
                ));
    }
}
