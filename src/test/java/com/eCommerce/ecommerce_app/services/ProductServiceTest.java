package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.exceptions.ProductAlreadyExistsException;
import com.eCommerce.ecommerce_app.requests.ProductRequestDto;
import com.eCommerce.ecommerce_app.respositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequestDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new ProductRequestDto();
        validDto.setName("Test Product");
        validDto.setPrice(BigDecimal.valueOf(100.00));
        validDto.setVat(BigDecimal.valueOf(23.00));
    }

    @Test
    void addProduct_ShouldSaveAndReturnProduct_WhenNameDoesNotExist() {
        // given
        when(productRepository.existsByName(validDto.getName())).thenReturn(false);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Product savedProduct = productService.addProduct(validDto);

        // then
        assertNotNull(savedProduct);
        assertEquals(validDto.getName(), savedProduct.getName());
        assertEquals(validDto.getPrice(), savedProduct.getPrice());
        assertEquals(validDto.getVat(), savedProduct.getVat());

        verify(productRepository).existsByName(validDto.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowException_WhenNameAlreadyExists() {
        // given
        when(productRepository.existsByName(validDto.getName())).thenReturn(true);

        // when + then
        ProductAlreadyExistsException exception = assertThrows(ProductAlreadyExistsException.class,
                () -> productService.addProduct(validDto));

        assertEquals("A product with this name already exists.", exception.getMessage());

        verify(productRepository).existsByName(validDto.getName());
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_ShouldThrowException_WhenDtoIsNull() {
        // when + then
        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(null));

        verify(productRepository, never()).existsByName(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_ShouldCalculateGrossPriceCorrectly() {
        ProductRequestDto dto = new ProductRequestDto();
        dto.setName("Test Product");
        dto.setPrice(BigDecimal.valueOf(100.00));
        dto.setVat(BigDecimal.valueOf(23.00));

        when(productRepository.existsByName(dto.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product savedProduct = productService.addProduct(dto);

        BigDecimal expectedGrossPrice = BigDecimal.valueOf(123.00);
        assertEquals(0, expectedGrossPrice.compareTo(savedProduct.getPriceGorss()),
                "Cena brutto powinna być poprawnie obliczona");

        verify(productRepository).save(any(Product.class));
    }
}
