package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.requests.ProductRequestDto;
import com.eCommerce.ecommerce_app.responses.ProductResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import com.eCommerce.ecommerce_app.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private AuthService authService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ProductController productController;

    private ProductRequestDto validDto;
    private User adminUser;
    private User normalUser;
    private Product product;

    private final String adminToken = "Bearer valid-admin-token";
    private final String userToken = "Bearer valid-user-token";
    private final String invalidToken = "Bearer invalid-token";

    @BeforeEach
    void setUp() {
        validDto = new ProductRequestDto();
        validDto.setName("Test Product");
        validDto.setPrice(BigDecimal.valueOf(100.0));
        validDto.setVat(BigDecimal.valueOf(23));

        adminUser = new User();
        adminUser.getRoles().add(Role.ADMIN);

        normalUser = new User();
        normalUser.getRoles().add(Role.USER);

        product = new Product();
        product.setId(1L);
        product.setName(validDto.getName());
        product.setPrice(validDto.getPrice());
        product.setVat(validDto.getVat());
    }

    @Test
    void addProduct_ShouldReturnCreated_WhenValidDataAndAdminToken() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.getUserByToken(adminToken)).thenReturn(adminUser);
        when(productService.addProduct(validDto)).thenReturn(product);

        ResponseEntity<ProductResponseDto> response = productController.addProduct(adminToken, validDto, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getId(), response.getBody().getId());
        assertEquals(product.getName(), response.getBody().getName());
        assertEquals(product.getPrice(), response.getBody().getPrice());
        assertEquals(product.getVat(), response.getBody().getVat());
        assertEquals("Product added successfully", response.getBody().getMessage());

        verify(authService).getUserByToken(adminToken);
        verify(productService).addProduct(validDto);
        verify(bindingResult).hasErrors();
    }

    @ParameterizedTest
    @MethodSource("provideValidationErrors")
    void addProduct_ShouldReturnBadRequest_ForVariousValidationErrors(List<ObjectError> errors) {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<ProductResponseDto> response = productController.addProduct(adminToken, validDto, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));

        for (ObjectError error : errors) {
            assertTrue(response.getBody().getMessage().contains(error.getDefaultMessage()));
        }

        verify(bindingResult).hasErrors();
        verify(bindingResult).getAllErrors();

        verifyNoInteractions(authService);
        verifyNoInteractions(productService);
    }

    static Stream<List<ObjectError>> provideValidationErrors() {
        return Stream.of(
                List.of(new ObjectError("name", "Product name cannot be empty")),
                List.of(new ObjectError("price", "Price must be positive")),
                List.of(new ObjectError("vat", "VAT must be provided")),
                List.of(
                        new ObjectError("name", "Product name cannot be empty"),
                        new ObjectError("price", "Price must be positive")
                )
        );
    }

    @Test
    void addProduct_ShouldReturnUnauthorized_WhenUserNotFoundByToken() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.getUserByToken(invalidToken)).thenReturn(null);

        ResponseEntity<ProductResponseDto> response = productController.addProduct(invalidToken, validDto, bindingResult);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Unauthorized"));

        verify(authService).getUserByToken(invalidToken);
        verifyNoInteractions(productService);
        verify(bindingResult).hasErrors();
    }

    @Test
    void addProduct_ShouldReturnForbidden_WhenUserDoesNotHaveAdminRole() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.getUserByToken(userToken)).thenReturn(normalUser);

        ResponseEntity<ProductResponseDto> response = productController.addProduct(userToken, validDto, bindingResult);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Access denied"));

        verify(authService).getUserByToken(userToken);
        verifyNoInteractions(productService);
        verify(bindingResult).hasErrors();
    }

}