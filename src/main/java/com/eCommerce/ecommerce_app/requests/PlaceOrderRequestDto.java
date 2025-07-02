package com.eCommerce.ecommerce_app.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequestDto {

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDto> items;

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
