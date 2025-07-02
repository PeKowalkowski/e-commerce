package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.Order;
import com.eCommerce.ecommerce_app.entities.OrderItem;
import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.exceptions.InsufficientStockException;
import com.eCommerce.ecommerce_app.exceptions.OrderNotFoundException;
import com.eCommerce.ecommerce_app.exceptions.ProductNotFoundException;
import com.eCommerce.ecommerce_app.requests.OrderItemRequestDto;
import com.eCommerce.ecommerce_app.requests.PlaceOrderRequestDto;
import com.eCommerce.ecommerce_app.responses.OrderDetailsResponseDto;
import com.eCommerce.ecommerce_app.responses.PlaceOrderResponseDto;
import com.eCommerce.ecommerce_app.respositories.OrderRepository;
import com.eCommerce.ecommerce_app.respositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("123456789");
        user.setCountry("Poland");
        user.setCity("Warsaw");
        user.setStreet("Main St 1");
        user.setPostalCode("00-001");

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");

        OrderItem item = new OrderItem();
        item.setId(100L);
        item.setProduct(product);
        item.setQuantity(2);
        item.setNetPrice(BigDecimal.valueOf(50));
        item.setGrossPrice(BigDecimal.valueOf(61.5));

        sampleOrder = new Order();
        sampleOrder.setId(5L);
        sampleOrder.setUser(user);
        sampleOrder.setOrderItems(List.of(item));
        sampleOrder.setTotalNetValue(BigDecimal.valueOf(100));
        sampleOrder.setTotalGrossValue(BigDecimal.valueOf(123));
    }
    //Place order
    @Test
    void placeOrder_ShouldPlaceOrderWithSingleProductSuccessfully() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Product1");
        product.setPrice(BigDecimal.valueOf(10));
        product.setPriceGorss(BigDecimal.valueOf(12.3));
        product.setQuantity(5);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(3);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        // when
        PlaceOrderResponseDto response = orderService.placeOrder(user, dto);

        // then
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals(BigDecimal.valueOf(30), response.getTotalNet());
        assertEquals(BigDecimal.valueOf(36.9), response.getTotalGross());
        assertEquals(1, response.getProductSummaries().size());
        assertEquals("Product1 x3", response.getProductSummaries().get(0));
        assertEquals("Order placed successfully", response.getMessage());

        verify(productRepository).save(argThat(p -> p.getQuantity() == 2));

        Order savedOrder = orderCaptor.getValue();
        assertEquals(user, savedOrder.getUser());
        assertEquals(BigDecimal.valueOf(30), savedOrder.getTotalNetValue());
        assertEquals(BigDecimal.valueOf(36.9), savedOrder.getTotalGrossValue());
        assertEquals(1, savedOrder.getOrderItems().size());
        assertEquals(3, savedOrder.getOrderItems().get(0).getQuantity());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldPlaceOrderWithMultipleProductsSuccessfully() {
        // given
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setPriceGorss(BigDecimal.valueOf(12.3));
        product1.setQuantity(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product2");
        product2.setPrice(BigDecimal.valueOf(5));
        product2.setPriceGorss(BigDecimal.valueOf(6.15));
        product2.setQuantity(7);

        OrderItemRequestDto item1 = new OrderItemRequestDto();
        item1.setProductId(1L);
        item1.setQuantity(4);

        OrderItemRequestDto item2 = new OrderItemRequestDto();
        item2.setProductId(2L);
        item2.setQuantity(3);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(item1, item2));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(200L);
            return o;
        });

        // when
        PlaceOrderResponseDto response = orderService.placeOrder(user, dto);

        // then
        assertNotNull(response);
        assertEquals(200L, response.getOrderId());

        BigDecimal expectedNet = BigDecimal.valueOf(40).add(BigDecimal.valueOf(15));
        BigDecimal expectedGross = BigDecimal.valueOf(49.2).add(BigDecimal.valueOf(18.45));
        assertEquals(0, expectedNet.compareTo(response.getTotalNet()));
        assertEquals(0, expectedGross.compareTo(response.getTotalGross()));

        assertEquals(2, response.getProductSummaries().size());
        assertTrue(response.getProductSummaries().contains("Product1 x4"));
        assertTrue(response.getProductSummaries().contains("Product2 x3"));
        assertEquals("Order placed successfully", response.getMessage());

        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));

        Order savedOrder = orderCaptor.getValue();
        assertEquals(user, savedOrder.getUser());
        assertEquals(2, savedOrder.getOrderItems().size());

        verify(productRepository).save(argThat(p -> p.getId() == 1L && p.getQuantity() == 6));
        verify(productRepository).save(argThat(p -> p.getId() == 2L && p.getQuantity() == 4));
    }

    @Test
    void placeOrder_ShouldThrowProductNotFoundException_WhenProductDoesNotExist() {
        // given
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(999L);
        itemDto.setQuantity(1);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> orderService.placeOrder(user, dto));
        assertEquals("Product ID not found: 999", ex.getMessage());

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldThrowInsufficientStockException_WhenNotEnoughStock() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Product1");
        product.setQuantity(2);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(5);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when + then
        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(user, dto));
        assertTrue(ex.getMessage().contains("Not enough stock for product"));

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldThrowRuntimeException_WhenUnexpectedErrorOccurs() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Product1");
        product.setPrice(BigDecimal.valueOf(10));
        product.setPriceGorss(BigDecimal.valueOf(12));
        product.setQuantity(10);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(1);

        PlaceOrderRequestDto dto = new PlaceOrderRequestDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB failure"));

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.placeOrder(user, dto));

        assertEquals("An unexpected error occurred while placing the order.", ex.getMessage());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any());
        verify(orderRepository, never()).save(any());
    }
    //Get order
    @Test
    void getOrderDetails_ShouldReturnDto_WhenOrderExists() {
        when(orderRepository.findById(5L)).thenReturn(Optional.of(sampleOrder));

        OrderDetailsResponseDto dto = orderService.getOrderDetails(5L);

        assertNotNull(dto);
        assertEquals(sampleOrder.getId(), dto.getOrderId());
        assertNotNull(dto.getCustomer());
        assertEquals(sampleOrder.getUser().getId(), dto.getCustomer().getId());
        assertEquals(sampleOrder.getUser().getUsername(), dto.getCustomer().getUsername());

        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());

        OrderDetailsResponseDto.OrderItemInfoDto itemDto = dto.getItems().get(0);
        assertEquals(10L, itemDto.getProductId());
        assertEquals("Test Product", itemDto.getProductName());
        assertEquals(2, itemDto.getQuantity());
        assertEquals(0, BigDecimal.valueOf(50).compareTo(itemDto.getNetPrice()));
        assertEquals(0, BigDecimal.valueOf(61.5).compareTo(itemDto.getGrossPrice()));

        assertEquals(0, sampleOrder.getTotalNetValue().compareTo(dto.getTotalNet()));
        assertEquals(0, sampleOrder.getTotalGrossValue().compareTo(dto.getTotalGross()));

        verify(orderRepository).findById(5L);
    }

    @Test
    void getOrderDetails_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderDetails(999L));

        assertEquals("Order with id 999 not found", ex.getMessage());

        verify(orderRepository).findById(999L);
    }

    @Test
    void getOrderDetails_ShouldThrowRuntimeException_WhenUnexpectedErrorOccurs() {
        when(orderRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.getOrderDetails(5L));

        assertEquals("An error occurred while fetching order details.", ex.getMessage());

        verify(orderRepository).findById(5L);
    }
}
