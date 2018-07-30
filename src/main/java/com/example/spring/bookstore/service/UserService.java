package com.example.spring.bookstore.service;

import com.example.spring.bookstore.data.entity.User;
import com.example.spring.bookstore.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
//        fillUsersRepository();
    }

    // Fills usersRepository with dummy users
    public void fillUsersRepository() {
        final String[] names = {
                "Yuri",
                "Ivan",
                "...$dd",
                "Petr.Ivanov"
        };

        for (String name : names) {
            if (User.isUserNameValid(name)) {
                User user = new User(name);
                userRepository.save(user);
                log.info("User added: id={}, name={}", user.getId(), user.getName());
            }
        }
    }

    public Iterable<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

}
