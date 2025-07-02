package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.requests.PlaceOrderRequestDto;
import com.eCommerce.ecommerce_app.responses.OrderDetailsResponseDto;
import com.eCommerce.ecommerce_app.responses.PlaceOrderResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import com.eCommerce.ecommerce_app.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponseDto> placeOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PlaceOrderRequestDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            PlaceOrderResponseDto response = new PlaceOrderResponseDto();
            response.setMessage("Validation failed: " + String.join(", ", errors));
            return ResponseEntity.badRequest().body(response);
        }

        User user = authService.getUserByToken(token);
        if (user == null) {
            PlaceOrderResponseDto response = new PlaceOrderResponseDto();
            response.setMessage("Unauthorized: invalid or missing token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        PlaceOrderResponseDto response = orderService.placeOrder(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get/{orderId}")
    public ResponseEntity<OrderDetailsResponseDto> getOrderDetails(@RequestHeader("Authorization") String token,
                                                                   @PathVariable Long orderId) {
        OrderDetailsResponseDto response = new OrderDetailsResponseDto();

        User user = authService.getUserByToken(token);
        if (user == null) {
            response.setMessage("Unauthorized: invalid or missing token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        OrderDetailsResponseDto orderDetails = orderService.getOrderDetails(orderId);

        boolean isAdmin = user.getRoles().contains(Role.ADMIN);
        boolean isOwner = orderDetails.getCustomer().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            response.setMessage("Access denied: cannot view others' orders.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(orderDetails);
    }
}
