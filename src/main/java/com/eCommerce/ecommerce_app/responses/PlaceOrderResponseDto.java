package com.eCommerce.ecommerce_app.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PlaceOrderResponseDto {

    private Long orderId;
    private String message;
    private BigDecimal totalNet;
    private BigDecimal totalGross;
    private List<String> productSummaries;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(BigDecimal totalNet) {
        this.totalNet = totalNet;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public List<String> getProductSummaries() {
        return productSummaries;
    }

    public void setProductSummaries(List<String> productSummaries) {
        this.productSummaries = productSummaries;
    }
}
