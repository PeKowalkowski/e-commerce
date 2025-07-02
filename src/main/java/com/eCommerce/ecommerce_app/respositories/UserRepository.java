package com.eCommerce.ecommerce_app.respositories;


import com.eCommerce.ecommerce_app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);


}
