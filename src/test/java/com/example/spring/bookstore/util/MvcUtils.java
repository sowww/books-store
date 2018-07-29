package com.example.spring.bookstore.util;

import com.google.gson.Gson;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

public class MvcUtils {

    private static final Gson gson = new Gson();

    public static <T> T mvcResultToClass(MvcResult result, Class<T> type) throws UnsupportedEncodingException {
        return gson.fromJson(result.getResponse().getContentAsString(), type);
    }
}
