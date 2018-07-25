package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.OrderRequest;
import com.example.spring.bookstore.services.OrderService;
import com.example.spring.bookstore.services.OrderService.OrderNotExistException;
import com.example.spring.bookstore.services.OrderService.OrderServiceFieldException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        // Getting orderService
        this.orderService = orderService;
    }

    // Creating a new order
    // example: POST /api/orders
    @PostMapping(value = "", produces = "application/json")
    public ResponseEntity<Object> createNewOrder(@Valid @RequestBody OrderRequest orderRequest, Errors errors) {

        // If there are not-valid params
        if (errors.hasErrors()) {
            // Create an errorView
            FieldErrorsView fieldErrorsView = new FieldErrorsView();
            fieldErrorsView.addErrors(errors);
            log.info("POST /api/orders errors count: {}", errors.getErrorCount());
            // End response with it (400) Bad Request
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.BAD_REQUEST);
        }

        try {
            // Trying to create an order
            Order order = orderService.createOrder(orderRequest);
            // If everything is ok, then response with created order
            return ResponseEntity.ok(OrderView.fromOrder(order));
        } catch (OrderServiceFieldException e) {
            // If we got an exception, then response with errorView (400) Bad Request
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    // Setting existing order status to PAID
    // example: POST /api/orders/12/pay
    @PostMapping(value = "/{id}/pay")
    public ResponseEntity<Object> orderSetPaidById(@PathVariable Long id) {

        try {
            // Trying to change order status
            Order order = orderService.orderSetPaidById(id);
            // If everything is ok, then response with changed order
            return ResponseEntity.ok(OrderView.fromOrder(order));
        } catch (OrderServiceFieldException e) {
            // If we got an exception, then response with errorView (400) Bad Request
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    // Getting all orders
    // example: GET /api/orders
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllOrders() {
        // Getting all orderViews from service
        return ResponseEntity.ok(OrderView.fromOrders(orderService.getAllOrders()));
    }

    // Getting an order by Id
    // example: GET /api/orders/12
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Getting order by id: {}", id);

        // Getting an order from service
        Optional<Order> order = orderService.getById(id);

        // If the order exists
        if (order.isPresent()) {
            // Returning the order
            return ResponseEntity.ok(OrderView.fromOrder(order.get()));
        } else {
            log.info("Order with id:{} not found", id);
            // Else returning (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // Getting all orders with userId
    // example: GET /api/orders/filter?userId=12
    @GetMapping(value = "/filter")
    public ResponseEntity<Object> getOrderByUserId(@RequestParam Long userId) {
        log.info("Getting orders by userId: {}", userId);

        //
        try {
            // Trying to get orders by userId
            Iterable<Order> orders = orderService.getOrdersByUserId(userId);
            // If everything is ok, then response with orderViews
            return ResponseEntity.ok(OrderView.fromOrders(orders));
        } catch (OrderServiceFieldException e) {
            // If we got an exception, then response with errorView
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }

    }

    // Deleting order by id
    // example: DELETE /api/orders/12
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteOrderById(@PathVariable Long id) {
        log.info("Deleting order by id: {}", id);
        try {
            // Trying to delete order by id
            orderService.deleteById(id);
            // If everything is ok, then response with (204) No Content
            return ResponseEntity.noContent().build();
        } catch (OrderNotExistException e) {
            // // If we got an exception, then response (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
