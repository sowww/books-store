package com.example.spring.bookstore.errorhandling;

import java.util.ArrayList;
import java.util.List;

public class ErrorsView {

    private List<RequestError> requestErrors;

    public ErrorsView() {
        this.requestErrors = new ArrayList<>();
    }

    public ErrorsView(String field, String errorMessage, Object rejectedValue) {
        requestErrors = new ArrayList<>();
        addError(field, errorMessage, rejectedValue);
    }

    public void addError(String field, String errorMessage, Object rejectedValue) {
        requestErrors.add(new RequestError(field, errorMessage, rejectedValue));
    }

    public List<RequestError> getRequestErrors() {
        return requestErrors;
    }
}
