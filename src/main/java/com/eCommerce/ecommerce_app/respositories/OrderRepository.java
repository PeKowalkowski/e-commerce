package com.eCommerce.ecommerce_app.respositories;

import com.eCommerce.ecommerce_app.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
