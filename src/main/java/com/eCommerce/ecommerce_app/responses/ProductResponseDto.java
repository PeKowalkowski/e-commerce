package com.eCommerce.ecommerce_app.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDto {

    public ProductResponseDto() {
    }

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal vat;
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
