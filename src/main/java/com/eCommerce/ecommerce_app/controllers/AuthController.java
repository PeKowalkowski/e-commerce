package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.requests.LoginRequestDto;
import com.eCommerce.ecommerce_app.requests.RegistrationRequestDto;
import com.eCommerce.ecommerce_app.responses.LoginResponseDto;
import com.eCommerce.ecommerce_app.responses.RegistrationResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")

public class AuthController {


    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> register(
            @Valid @RequestBody RegistrationRequestDto dto,
            BindingResult bindingResult) {

        RegistrationResponseDto response = new RegistrationResponseDto();

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            response.setErrors(errors);
            response.setMessage("Validation failed");
            return ResponseEntity.badRequest().body(response);
        }

        User user = authService.registerUser(dto);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Registration successful");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        LoginResponseDto response = authService.login(dto);
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: token is missing or invalid");
        }

        User user = authService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: token is missing or invalid");
        }
        authService.logout(token);
        return ResponseEntity.ok("Logout successful");
    }
}
