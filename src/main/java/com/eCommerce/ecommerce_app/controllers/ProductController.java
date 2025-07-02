package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.requests.ProductRequestDto;
import com.eCommerce.ecommerce_app.responses.ProductResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import com.eCommerce.ecommerce_app.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final AuthService authService;

    @Autowired
    public ProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ProductResponseDto> addProduct(@RequestHeader("Authorization") String token,
                                                         @Valid @RequestBody ProductRequestDto dto,
                                                         BindingResult bindingResult) {

        ProductResponseDto response = new ProductResponseDto();

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            response.setMessage("Validation failed: " + String.join(", ", errors));
            return ResponseEntity.badRequest().body(response);
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            response.setMessage("Unauthorized: invalid or missing token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!user.getRoles().contains(com.eCommerce.ecommerce_app.enums.Role.ADMIN)) {
            response.setMessage("Access denied: insufficient permissions.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Product product = productService.addProduct(dto);

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setVat(product.getVat());
        response.setMessage("Product added successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
