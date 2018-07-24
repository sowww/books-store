package com.example.spring.bookstore.errors;

import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

public class FieldErrorsView {

    private List<FieldError> fieldErrors;

    public FieldErrorsView() {
        this.fieldErrors = new ArrayList<>();
    }

    public FieldErrorsView(String field, String errorMessage, Object rejectedValue) {
        fieldErrors = new ArrayList<>();
        addError(field, errorMessage, rejectedValue);
    }

    public void addError(String field, String errorMessage, Object rejectedValue) {
        fieldErrors.add(new FieldError(field, errorMessage, rejectedValue));
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void addErrors(Errors errors) {
        for (org.springframework.validation.FieldError fieldError : errors.getFieldErrors()) {
            addError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue()
            );
        }
    }
}
