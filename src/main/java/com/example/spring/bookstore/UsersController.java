package com.example.spring.bookstore;

import com.example.spring.bookstore.data.entity.Order;
import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.view.OrderView;
import com.example.spring.bookstore.service.OrderService;
import com.example.spring.bookstore.service.UserService;
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

    /**
     * Getting all users
     * <p>example: GET /api/users</p>
     */
    @GetMapping(value = {"/", ""})
    public ResponseEntity<Object> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    /**
     * Getting user by id
     * <p>example: GET /api/users/1</p></>
     *
     * @param id user id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {

        Optional<User> user = userService.getById(id);
        if (user.isPresent()) {
            log.info("Get user: {}", user.get().getName());
            return ResponseEntity.ok(user);
        } else {
            log.info("User with id:{} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Getting all user orders
     * <p>example: GET /api/users/11/orders</p>
     *
     * @param id user id
     */
    @GetMapping("/{id}/orders")
    public ResponseEntity<Object> getOrdersByUserId(@PathVariable Long id) {
        log.debug("getOrdersByUserId {}", id);
        try {
            Iterable<Order> orders = orderService.getOrdersByUserId(id);
            return ResponseEntity.ok(OrderView.fromOrders(orders));
        } catch (OrderService.OrderServiceFieldException e) {
            return new ResponseEntity<>(e.getErrorsView(), HttpStatus.NOT_FOUND);
        }
    }

}
