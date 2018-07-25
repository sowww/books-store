package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.OrderRequest;
import com.example.spring.bookstore.services.OrderService;
import com.example.spring.bookstore.services.OrderService.OrderServiceFieldException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        // Getting orderService
        this.orderService = orderService;
    }

    // Creating a new view
    // example: POST /api/orders
    @PostMapping(value = "", produces = "application/json")
    public ResponseEntity<Object> createNewOrder(@Valid @RequestBody OrderRequest orderRequest, Errors errors) {

        if (errors.hasErrors()) {
            FieldErrorsView fieldErrorsView = new FieldErrorsView();
            fieldErrorsView.addErrors(errors);
            log.info("POST /api/orders errors count: {}", errors.getErrorCount());
            return new ResponseEntity<>(fieldErrorsView, HttpStatus.BAD_REQUEST);
        }

        try {
            Order order = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(OrderView.fromOrder(order));
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    // Setting existing view status to PAID
    // example: POST /api/orders/12/pay
    @PostMapping(value = "/{id}/pay")
    public ResponseEntity<Object> orderSetPaidById(@PathVariable Long id) {

        try {
            Order order = orderService.orderSetPaidById(id);
            return ResponseEntity.ok(OrderView.fromOrder(order));
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    // Getting all orders
    // example: GET /api/orders
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllOrders() {
        // Getting all orders from repo
//        Iterable<Order> orders = ordersRepository.findAll();
        Iterable<Order> orders = orderService.getAllOrders();
        Set<OrderView> orderViews = new HashSet<>();
        for (Order order : orders) {
            orderViews.add(OrderView.fromOrder(order));
        }
        return ResponseEntity.ok(orderViews);
    }

    // Getting an view by Id
    // example: GET /api/orders/12
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Getting view by id: {}", id);

        // Getting an view from service
        Optional<Order> order = orderService.getById(id);

        // If the view exists
        if (order.isPresent()) {
            // returning the view
            return ResponseEntity.ok(OrderView.fromOrder(order.get()));
        } else {
            // else
            log.info("Order with id:{} not found", id);
            // Returning (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // Getting all orders with userId
    // example: GET /api/orders/filter?userId=12
    @GetMapping(value = "/filter")
    public ResponseEntity<Object> getOrderByUserId(@RequestParam Long userId) {
        log.info("Getting orders by userId: {}", userId);

        try {
            Iterable<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }

    }

    // Deleting view by id
    // example: DELETE /api/orders/12
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteOrderById(@PathVariable Long id) {
        log.info("Deleting view by id: {}", id);
        // Getting an view from the repo
        Optional<Order> order = orderService.getById(id);

        // Checking if view exists
        if (order.isPresent()) {
            // If exists delete it from the repo
            orderService.deleteById(id);
            // and then returning (204) No Content
            return ResponseEntity.noContent().build();
        } else {
            // Else set status to (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
