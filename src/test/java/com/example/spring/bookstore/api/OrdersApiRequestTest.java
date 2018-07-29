package com.example.spring.bookstore.api;


import com.example.spring.bookstore.OrdersController;
import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.BookItem;
import com.example.spring.bookstore.request.objects.OrderRequest;
import com.example.spring.bookstore.service.BookService;
import com.example.spring.bookstore.service.OrderService;
import com.example.spring.bookstore.service.UserService;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = OrdersController.class)
public class OrdersApiRequestTest {

    private static final Logger log = LoggerFactory.getLogger(OrdersApiRequestTest.class);

    private static final String USER_NAME = "UserName";
    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 2L;
    private static final Long BOOK_ID = 3L;
    private static final int BOOK_QUANTITY = 5;
    @Autowired
    MockMvc mvc;
    @MockBean
    OrderService orderService;
    @MockBean
    BookService bookService;
    @MockBean
    UserService userService;
    private Gson gson;
    private User user;
    private Order order;
    private Set<BookItem> bookItems;
    private OrderRequest orderRequest;


    @Before
    public void prepare() {
        gson = new Gson();
        user = new User(USER_NAME);
        setField(user, "id", USER_ID);
        order = new Order(
                user,
                1000D,
                new HashSet<>(),
                Order.Status.PENDING
        );
        setField(order, "orderId", ORDER_ID);

        bookItems = new HashSet<>();
        bookItems.add(new BookItem(BOOK_ID, BOOK_QUANTITY));
        orderRequest = new OrderRequest(bookItems, USER_ID);
    }

    @Test
    public void deletingOrderReturnNoContent() throws Exception {
        mvc.perform(
                delete("/api/orders/1").accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isNoContent());

    }

    @Test
    public void deletingNonExistentOrderReturnNotFound() throws Exception {
        doThrow(new OrderService.OrderNotExistException()).when(orderService).deleteById(1L);
        mvc.perform(
                delete("/api/orders/1").accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllOrdersReturnOrders() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderService.getAllOrders()).thenReturn(orders);
        mvc.perform(
                get("/api/orders").accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(USER_ID.intValue())))
                .andExpect(jsonPath("$[0].orderId", is(ORDER_ID.intValue())));
    }

    @Test
    public void getOrderReturnOrder() throws Exception {
        when(orderService.getById(ORDER_ID))
                .thenReturn(java.util.Optional.ofNullable(order));
        mvc.perform(
                get("/api/orders/" + ORDER_ID).accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(USER_ID.intValue())))
                .andExpect(jsonPath("$.orderId", is(ORDER_ID.intValue())));
    }

    @Test
    public void getNonExistentOrderReturnNotFound() throws Exception {
        when(orderService.getById(ORDER_ID))
                .thenReturn(Optional.empty());
        mvc.perform(
                get("/api/orders/" + ORDER_ID).accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void getOrdersUserReturnIsOk() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderService.getOrdersByUserId(USER_ID)).thenReturn(orders);
        mvc.perform(
                get("/api/orders/filter?userId=" + USER_ID)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }

    @Test
    public void getOrdersOfNonExistentUserReturnNotFound() throws Exception {
        FieldErrorsView errorsView = new FieldErrorsView("", "", 1);
        when(orderService.getOrdersByUserId(USER_ID))
                .thenThrow(new OrderService.OrderServiceFieldException(errorsView));
        mvc.perform(
                get("/api/orders/filter?userId=" + USER_ID)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void createOrderReturnOrder() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(order);

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(ORDER_ID.intValue())));
    }

    @Test
    public void createOrderWithValidationErrorsReturnBadRequest() throws Exception {
        Set<BookItem> bookItems = new HashSet<>();
        bookItems.add(new BookItem(10L, -1));
        bookItems.add(new BookItem(11L, -1));
        OrderRequest orderRequest = new OrderRequest(bookItems, USER_ID);
        log.info("orderRequest {}", gson.toJson(orderRequest));

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createOrderWithExceptionReturnBadRequest() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new OrderService.OrderServiceFieldException(
                                new FieldErrorsView("", "", null)
                        )
                );

        mvc.perform(post("/api/orders")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void settingPaidStatusReturnOrder() throws Exception {
        when(orderService.orderSetPaidById(any()))
                .thenReturn(order);

        mvc.perform(post("/api/orders/" + ORDER_ID + "/pay")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(ORDER_ID.intValue())));
    }

    @Test
    public void settingPaidStatusWithExceptionReturnBadRequest() throws Exception {
        when(orderService.orderSetPaidById(any()))
                .thenThrow(new OrderService.OrderServiceFieldException(
                        new FieldErrorsView("", "", 1)
                ));

        mvc.perform(post("/api/orders/" + ORDER_ID + "/pay")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(orderRequest))
        )
                .andExpect(status().isBadRequest());
    }
}
