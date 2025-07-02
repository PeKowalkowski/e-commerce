package com.eCommerce.ecommerce_app.config;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.respositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setPhoneNumber("123456789");
            admin.setCountry("Poland");
            admin.setCity("Warsaw");
            admin.setStreet("Admin Street 1");
            admin.setPostalCode("00-001");
            admin.getRoles().add(Role.ADMIN);

            userRepository.save(admin);
        }
    }
}
