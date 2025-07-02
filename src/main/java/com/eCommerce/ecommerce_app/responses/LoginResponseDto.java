package com.eCommerce.ecommerce_app.responses;

import lombok.Data;

@Data
public class LoginResponseDto {


    public LoginResponseDto(String token, String message) {
        this.token = token;
        this.message = message;
    }

    private String token;
    private String message;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
