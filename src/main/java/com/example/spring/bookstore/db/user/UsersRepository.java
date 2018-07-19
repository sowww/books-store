package com.example.spring.bookstore.db.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<String> getNameById(Long id);
}
