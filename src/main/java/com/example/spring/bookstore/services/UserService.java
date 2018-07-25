package com.example.spring.bookstore.services;

import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                userRepository.save(user);
                log.info("User added: id={}, name={}", user.getId(), user.getName());
            }
        }
    }
}
