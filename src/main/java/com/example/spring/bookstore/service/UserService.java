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
    }

    /**
     * Fills usersRepository with dummy users
     *
     * @return all users
     */
    public Iterable<User> fillUsersRepository() {
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
        return userRepository.findAll();
    }


    /**
     * Get all users from repo
     *
     * @return all users
     */
    public Iterable<User> getAll() {
        return userRepository.findAll();
    }


    /**
     * Get user by id
     *
     * @param id user id
     * @return user
     */
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }


    /**
     * Delete all users from repo
     */
    public void deleteAll() {
        userRepository.deleteAll();
    }


    /**
     * Add new user to repo
     *
     * @param user user to add
     * @return created user
     */
    public User addUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Delete user by id
     *
     * @param id user id
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
