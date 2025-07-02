
package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.requests.OrderItemRequestDto;
import com.eCommerce.ecommerce_app.requests.PlaceOrderRequestDto;
import com.eCommerce.ecommerce_app.responses.PlaceOrderResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import com.eCommerce.ecommerce_app.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private OrderService orderService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private OrderController orderController;

    private PlaceOrderRequestDto validDto;
    private User user;

    private final String validToken = "Bearer valid-token";
    private final String invalidToken = "Bearer invalid-token";

    @BeforeEach
    void setUp() {
        OrderItemRequestDto item1 = new OrderItemRequestDto();
        item1.setProductId(1L);
        item1.setQuantity(2);

        validDto = new PlaceOrderRequestDto();
        validDto.setItems(List.of(item1));

        user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setRoles(Set.of(Role.USER));
    }

    @Test
    void placeOrder_ShouldReturnCreated_WhenValidOrderAndValidTokenAndNoValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.getUserByToken(validToken)).thenReturn(user);

        PlaceOrderResponseDto serviceResponse = new PlaceOrderResponseDto();
        serviceResponse.setOrderId(100L);
        serviceResponse.setTotalNet(BigDecimal.valueOf(200.0));
        serviceResponse.setTotalGross(BigDecimal.valueOf(246.0));
        serviceResponse.setMessage("Order placed successfully");

        when(orderService.placeOrder(user, validDto)).thenReturn(serviceResponse);

        ResponseEntity<PlaceOrderResponseDto> response = orderController.placeOrder(validToken, validDto, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(serviceResponse.getOrderId(), response.getBody().getOrderId());
        assertEquals(0, serviceResponse.getTotalNet().compareTo(response.getBody().getTotalNet()));
        assertEquals(0, serviceResponse.getTotalGross().compareTo(response.getBody().getTotalGross()));
        assertEquals(serviceResponse.getMessage(), response.getBody().getMessage());

        verify(authService).getUserByToken(validToken);
        verify(orderService).placeOrder(user, validDto);
        verify(bindingResult).hasErrors();
    }

    @Test
    void placeOrder_ShouldReturnBadRequest_WhenValidationErrorsExist() {
        when(bindingResult.hasErrors()).thenReturn(true);

        List<ObjectError> errors = List.of(
                new ObjectError("productId", "Product ID cannot be null"),
                new ObjectError("quantity", "Quantity must be at least 1")
        );
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<PlaceOrderResponseDto> response = orderController.placeOrder(validToken, validDto, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        for (ObjectError error : errors) {
            assertTrue(response.getBody().getMessage().contains(error.getDefaultMessage()));
        }

        verify(bindingResult).hasErrors();
        verify(bindingResult).getAllErrors();

        verifyNoInteractions(authService);
        verifyNoInteractions(orderService);
    }

    @Test
    void placeOrder_ShouldReturnUnauthorized_WhenUserNotFoundByToken() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.getUserByToken(invalidToken)).thenReturn(null);

        ResponseEntity<PlaceOrderResponseDto> response = orderController.placeOrder(invalidToken, validDto, bindingResult);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Unauthorized"));

        verify(authService).getUserByToken(invalidToken);
        verify(bindingResult).hasErrors();
        verifyNoInteractions(orderService);
    }
    @Test
    void placeOrder_ShouldReturnBadRequest_WhenProductIdOrQuantityIsMissingOrInvalid() {
        OrderItemRequestDto invalidItem = new OrderItemRequestDto();
        invalidItem.setProductId(null);
        invalidItem.setQuantity(0);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(invalidItem));

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("productId", "Product ID must be provided"),
                new ObjectError("quantity", "Quantity must be at least 1")
        ));

        ResponseEntity<PlaceOrderResponseDto> response = orderController.placeOrder("valid-token", dto, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertTrue(response.getBody().getMessage().contains("Product ID must be provided"));
        assertTrue(response.getBody().getMessage().contains("Quantity must be at least 1"));

        verify(bindingResult).hasErrors();
        verify(bindingResult).getAllErrors();
        verifyNoInteractions(authService);
        verifyNoInteractions(orderService);
    }
}
