package com.eCommerce.ecommerce_app.respositories;

import com.eCommerce.ecommerce_app.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
}
