package com.example.spring.bookstore;

import com.example.spring.bookstore.db.user.User;
import com.example.spring.bookstore.db.user.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final Logger log = LoggerFactory.getLogger(UsersController.class);
    private final UsersRepository usersRepository;

    public UsersController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        fillUsersRepository();
    }

    private void fillUsersRepository() {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("Yuri"));
        users.add(new User("Ivan"));

        for (User user : users) {
            usersRepository.save(user);
            log.info("User added: id={}, name={}", user.getId(), user.getName());
        }
    }

    @GetMapping("/")
    public Iterable<User> getAllUsers() {
        Iterable<User> users = usersRepository.findAll();
        log.info("Get All users: {}", users.spliterator().getExactSizeIfKnown());
        return users;
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        Optional<User> user = usersRepository.findById(id);
        user.ifPresent(user1 -> log.info("Get user: {}", user1.getName()));
        return user;
    }


}
