package com.example.spring.bookstore;

import com.example.spring.bookstore.db.user.User;
import com.example.spring.bookstore.db.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
        fillUserRepository();
    }

    private void fillUserRepository() {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("Yuri"));
        users.add(new User("Ivan"));

        for (User user : users) {
            userRepository.save(user);
            log.info("User added: id={}, name={}", user.getId(), user.getName());
        }
    }

    @GetMapping("/all")
    public Iterable<User> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        log.info("Get All users: {}", users.spliterator().getExactSizeIfKnown());
        return users;
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        log.info("Get All users: {}", user.get().getName());
        return user;
    }


}
