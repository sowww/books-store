package com.example.spring.bookstore.data.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ValidationException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
public class BookTest {

    @Test
    public void cantCreateBookWithNonValidName() {
        try {
            new Book("%Book", 100D, 1);
            fail();
        } catch (ValidationException e) {
            assertThat(e.getMessage(), containsString("Book name"));
        }
    }

    @Test
    public void cantCreateBookWithNonValidPrice() {
        try {
            new Book("Book", -100D, 1);
            fail();
        } catch (ValidationException e) {
            assertThat(e.getMessage(), containsString("price"));
        }
    }

    @Test
    public void cantCreateBookWithNonValidQuantity() {
        try {
            new Book("Book", 100D, -1);
            fail();
        } catch (ValidationException e) {
            assertThat(e.getMessage(), containsString("quantity"));
        }
    }
}