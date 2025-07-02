package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.Product;
import com.eCommerce.ecommerce_app.exceptions.ProductAlreadyExistsException;
import com.eCommerce.ecommerce_app.requests.ProductRequestDto;
import com.eCommerce.ecommerce_app.respositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(ProductRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ProductRequestDto cannot be null");
        }
        try {
            if (productRepository.existsByName(dto.getName())) {
                log.warn("Product with name '{}' already exists.", dto.getName());
                throw new ProductAlreadyExistsException("A product with this name already exists.");
            }

            Product product = new Product();
            product.setName(dto.getName());
            product.setPrice(dto.getPrice());
            product.setVat(dto.getVat());

            BigDecimal vatFraction = product.getVat().divide(BigDecimal.valueOf(100));
            BigDecimal priceGross = product.getPrice().add(product.getPrice().multiply(vatFraction));
            product.setPriceGorss(priceGross);

            Product saved = productRepository.save(product);
            log.info("Product added: {}", saved.getName());
            return saved;

        } catch (ProductAlreadyExistsException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while adding product", ex);
            throw new RuntimeException("An error occurred while adding the product.");
        }
    }
}
