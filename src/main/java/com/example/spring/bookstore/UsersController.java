package com.example.spring.bookstore;

import com.example.spring.bookstore.db.user.User;
import com.example.spring.bookstore.db.user.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UsersRepository usersRepository;

    public UsersController(UsersRepository usersRepository) {
        // Getting usersRepository
        this.usersRepository = usersRepository;
        // Filling it with some users
        fillUsersRepository();
    }

    // Fills usersRepository with dummy users
    private void fillUsersRepository() {
        final String[] names = {
                "Yuri",
                "Ivan",
                "...$dd",
                "Petr.Ivanov"
        };

        // for every name from array
        for (String name : names) {
            // Check if name is valid
            if (User.isUserNameValid(name)) {
                // If it's valid, then create new user
                User user = new User(name);
                // Add him to repo
                usersRepository.save(user);
                log.info("User added: id={}, name={}", user.getId(), user.getName());
            }
        }
    }

    // Getting all users
    // example: GET /api/users
    @GetMapping(value = {"/", ""})
    public ResponseEntity<Object> getAllUsers() {
        // Get all users from repo
        Iterable<User> users = usersRepository.findAll();
        log.info("Getting all users");
        return ResponseEntity.ok(users);
    }

    // Getting user by id
    // example: GET /api/users/1
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        // Getting user from repo
        Optional<User> user = usersRepository.findById(id);
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

}
