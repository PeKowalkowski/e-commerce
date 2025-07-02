package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.Order;
import com.eCommerce.ecommerce_app.entities.OrderItem;
import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.exceptions.InsufficientStockException;
import com.eCommerce.ecommerce_app.exceptions.ProductNotFoundException;
import com.eCommerce.ecommerce_app.requests.OrderItemRequestDto;
import com.eCommerce.ecommerce_app.requests.PlaceOrderRequestDto;
import com.eCommerce.ecommerce_app.responses.PlaceOrderResponseDto;
import com.eCommerce.ecommerce_app.respositories.OrderRepository;
import com.eCommerce.ecommerce_app.respositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PlaceOrderResponseDto placeOrder(User user, PlaceOrderRequestDto dto) {
        try {
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalNet = BigDecimal.ZERO;
            BigDecimal totalGross = BigDecimal.ZERO;
            List<String> summaries = new ArrayList<>();

            for (OrderItemRequestDto itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product ID not found: " + itemDto.getProductId()));

                if (product.getQuantity() < itemDto.getQuantity()) {
                    throw new InsufficientStockException("Not enough stock for product: " + product.getName());
                }

                product.setQuantity(product.getQuantity() - itemDto.getQuantity());
                productRepository.save(product);

                BigDecimal net = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                BigDecimal gross = product.getPriceGorss().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setNetPrice(net);
                orderItem.setGrossPrice(gross);
                orderItems.add(orderItem);

                totalNet = totalNet.add(net);
                totalGross = totalGross.add(gross);

                summaries.add(product.getName() + " x" + itemDto.getQuantity());
            }

            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalNetValue(totalNet);
            order.setTotalGrossValue(totalGross);

            for (OrderItem item : orderItems) {
                item.setOrder(order);
            }

            order.setOrderItems(orderItems);
            Order saved = orderRepository.save(order);

            PlaceOrderResponseDto response = new PlaceOrderResponseDto();
            response.setOrderId(saved.getId());
            response.setTotalNet(totalNet);
            response.setTotalGross(totalGross);
            response.setProductSummaries(summaries);
            response.setMessage("Order placed successfully");

            return response;
        } catch (ProductNotFoundException | InsufficientStockException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while placing order", ex);
            throw new RuntimeException("An unexpected error occurred while placing the order.");
        }
    }
}
