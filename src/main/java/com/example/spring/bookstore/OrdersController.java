package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.errors.FieldErrorsView;
import com.example.spring.bookstore.request.objects.OrderRequest;
import com.example.spring.bookstore.service.OrderService;
import com.example.spring.bookstore.service.OrderService.OrderNotExistException;
import com.example.spring.bookstore.service.OrderService.OrderServiceFieldException;
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
        this.orderService = orderService;
    }

    /**
     * Creating a new order
     * <p>example: POST /api/orders</p>
     *
     * @param orderRequest request with items
     * @param errors       validation errors
     */
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
            return new ResponseEntity<>(OrderView.fromOrder(order), HttpStatus.CREATED);
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Setting existing order status to PAID
     * <p>example: POST /api/orders/12/pay</p>
     *
     * @param id order id
     */
    @PostMapping(value = "/{id}/pay")
    public ResponseEntity<Object> orderSetPaidById(@PathVariable Long id) {

        try {
            Order order = orderService.orderSetPaidById(id);
            return ResponseEntity.ok(OrderView.fromOrder(order));
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Getting all orders
     * <p>example: GET /api/orders</p>
     */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAllOrders() {
        return ResponseEntity.ok(OrderView.fromOrders(orderService.getAllOrders()));
    }

    /**
     * Getting an order by Id
     * <p>example: GET /api/orders/12</p>
     *
     * @param id order id
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Getting order by id: {}", id);

        Optional<Order> order = orderService.getById(id);

        if (order.isPresent()) {
            return ResponseEntity.ok(OrderView.fromOrder(order.get()));
        } else {
            log.info("Order with id:{} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Getting all orders with userId
     * <p>example: GET /api/orders/filter?userId=12</p>
     *
     * @param userId user id
     */
    @GetMapping(value = "/filter")
    public ResponseEntity<Object> getOrderByUserId(@RequestParam Long userId) {
        log.info("Getting orders by userId: {}", userId);

        try {
            Iterable<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(OrderView.fromOrders(orders));
        } catch (OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Deleting order by id
     * <p>example: DELETE /api/orders/12</p>
     *
     * @param id order id
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteOrderById(@PathVariable Long id) {
        log.info("Deleting order by id: {}", id);
        try {
            orderService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (OrderNotExistException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
