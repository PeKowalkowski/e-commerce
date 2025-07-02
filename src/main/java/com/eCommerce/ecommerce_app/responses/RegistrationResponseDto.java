package com.eCommerce.ecommerce_app.responses;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationResponseDto {
    public RegistrationResponseDto() {
    }

    public RegistrationResponseDto(Long id, String username, String email, String message, List<String> errors) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.message = message;
        this.errors = errors;
    }

    private Long id;
    private String username;
    private String email;
    private String message;
    private List<String> errors;


    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
