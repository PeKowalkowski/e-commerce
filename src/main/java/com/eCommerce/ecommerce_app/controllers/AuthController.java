package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.requests.RegistrationRequestDto;
import com.eCommerce.ecommerce_app.responses.RegistrationResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")

public class AuthController {


    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> register(@Valid @RequestBody RegistrationRequestDto dto) {
        User user = authService.registerUser(dto);
        RegistrationResponseDto response = new RegistrationResponseDto(user.getId(), user.getUsername(), user.getEmail(), "Registartion successful.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
