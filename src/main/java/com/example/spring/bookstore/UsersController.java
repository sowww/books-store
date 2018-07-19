package com.example.spring.bookstore;

import com.example.spring.bookstore.db.user.User;
import com.example.spring.bookstore.db.user.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
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
                // then create new user
                User user = new User(name);
                // Add him to repo
                usersRepository.save(user);
                log.info("User added: id={}, name={}", user.getId(), user.getName());
            }
        }
    }

    // Getting all users; GET /api/users
    @GetMapping(value = {"/", ""})
    public Iterable<User> getAllUsers(HttpServletResponse response) {
        // Get all users from repo
        Iterable<User> users = usersRepository.findAll();
        long usersCount = users.spliterator().getExactSizeIfKnown();
        if (usersCount > 0) {
            response.addHeader("message", "Users are found");
            log.info("Get All users: {}", usersCount);
            return users;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "Users are not found");
            log.info("Get All users: Users are not found");
            return null;
        }
    }

    // Getting user by id ; GET /api/users/1
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id,
                                      HttpServletResponse response) {
        // Getting user from repo
        Optional<User> user = usersRepository.findById(id);
        // If user doesn't exist
        if (!user.isPresent()) {
            log.info("User with id:{} not found", id);
            // Setting status to 404 and returning nothing
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader("message", "User is not found");
            return Optional.empty();
        }
        log.info("Get user: {}", user.get().getName());
        return user;
    }

}
