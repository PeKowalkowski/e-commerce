package com.eCommerce.ecommerce_app.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
