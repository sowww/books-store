package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.repository.OrderRepository;
import com.example.spring.bookstore.data.repository.UserRepository;
import com.example.spring.bookstore.data.view.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final Logger log = LoggerFactory.getLogger(UsersController.class);
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public UsersController(UserRepository userRepository, OrderRepository orderRepository) {
        // Getting usersRepository and ordersRepository
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }


    // Getting all users
    // example: GET /api/users
    @GetMapping(value = {"/", ""})
    public ResponseEntity<Object> getAllUsers() {
        // Get all users from repo
        Iterable<User> users = userRepository.findAll();
        log.info("Getting all users");
        return ResponseEntity.ok(users);
    }

    // Getting user by id
    // example: GET /api/users/1
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        // Getting user from repo
        Optional<User> user = userRepository.findById(id);
        // If the user exists
        if (user.isPresent()) {
            log.info("Get user: {}", user.get().getName());
            // Return the user
            return ResponseEntity.ok(user);
        } else {
            log.info("User with id:{} not found", id);
            // If user doesn't exist respond with 404
            return ResponseEntity.notFound().build();
        }
    }

    // Getting all user orders
    // example: GET /api/users/11/orders
    @GetMapping("/{id}/orders")
    public ResponseEntity<Object> getOrdersByUserId(@PathVariable Long id) {
        // Getting user from repo
        Optional<User> user = userRepository.findById(id);
        // If the user exists
        if (user.isPresent()) {
            // Creating view views set
            Set<OrderView> orderViews = new HashSet<>();
            // And filling it
            for (Order order : orderRepository.getOrdersByUserId(user.get().getId())) {
                orderViews.add(OrderView.fromOrder(order));
            }
            // Return the view views
            return ResponseEntity.ok(orderViews);
        } else {
            log.info("User with id:{} not found", id);
            // If user doesn't exist respond with 404
            return ResponseEntity.notFound().build();
        }
    }

}
