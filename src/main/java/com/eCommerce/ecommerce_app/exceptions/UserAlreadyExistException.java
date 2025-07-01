package com.eCommerce.ecommerce_app.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String email) {
        super("User with email " + email + " already exists");
    }
}