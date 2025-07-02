package com.eCommerce.ecommerce_app.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailsResponseDto {

    private Long orderId;

    private CustomerInfoDto customer;

    private List<OrderItemInfoDto> items;

    private BigDecimal totalNet;

    private BigDecimal totalGross;
    private String message;

    @Data
    public static class CustomerInfoDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String country;
        private String city;
        private String street;
        private String postalCode;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }

    @Data
    public static class OrderItemInfoDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal netPrice;
        private BigDecimal grossPrice;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getNetPrice() {
            return netPrice;
        }

        public void setNetPrice(BigDecimal netPrice) {
            this.netPrice = netPrice;
        }

        public BigDecimal getGrossPrice() {
            return grossPrice;
        }

        public void setGrossPrice(BigDecimal grossPrice) {
            this.grossPrice = grossPrice;
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public CustomerInfoDto getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfoDto customer) {
        this.customer = customer;
    }

    public List<OrderItemInfoDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemInfoDto> items) {
        this.items = items;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
