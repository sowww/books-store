package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.services.OrderService;
import com.example.spring.bookstore.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final Logger log = LoggerFactory.getLogger(UsersController.class);
    private final UserService userService;
    private final OrderService orderService;

    public UsersController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    // Getting all users
    // example: GET /api/users
    @GetMapping(value = {"/", ""})
    public ResponseEntity<Object> getAllUsers() {
        // Getting all users from service and response with them
        return ResponseEntity.ok(userService.getAll());
    }

    // Getting user by id
    // example: GET /api/users/1
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {

        // Getting the user from service
        Optional<User> user = userService.getById(id);
        // If the user exists
        if (user.isPresent()) {
            log.info("Get user: {}", user.get().getName());
            // Return the user
            return ResponseEntity.ok(user);
        } else {
            log.info("User with id:{} not found", id);
            // If user doesn't exist respond with (404) Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // Getting all user orders
    // example: GET /api/users/11/orders
    @GetMapping("/{id}/orders")
    public ResponseEntity<Object> getOrdersByUserId(@PathVariable Long id) {
        try {
            // Trying to get orders
            Iterable<Order> orders = orderService.getOrdersByUserId(id);
            return ResponseEntity.ok(OrderView.fromOrders(orders));
        } catch (OrderService.OrderServiceFieldException e) {
            // if user with this userId doesn't exist
            // Response with errorView
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.BAD_REQUEST);
        }
    }

}
