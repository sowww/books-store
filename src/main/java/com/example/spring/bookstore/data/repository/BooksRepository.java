package com.example.spring.bookstore.data.repository;

import com.example.spring.bookstore.data.entity.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BooksRepository extends CrudRepository<Book, Long> {
}
