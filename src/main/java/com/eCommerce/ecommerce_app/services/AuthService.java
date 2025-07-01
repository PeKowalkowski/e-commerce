package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.exceptions.UserAlreadyExistException;
import com.eCommerce.ecommerce_app.requests.RegistrationRequestDto;
import com.eCommerce.ecommerce_app.respositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegistrationRequestDto dto) {
        try {
            if (userRepository.existsByEmail(dto.getEmail())) {
                log.error("Registration failed: user with email " + dto.getEmail() + " already exists.");
                throw new UserAlreadyExistException("A user with this email already exists");
            }

            User newUser = new User();
            newUser.setFirstName(dto.getFirstName());
            newUser.setLastName(dto.getLastName());
            newUser.setUsername(dto.getUsername());
            newUser.setEmail(dto.getEmail());
            newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
            newUser.setPhoneNumber(dto.getPhoneNumber());
            newUser.setCountry(dto.getCountry());
            newUser.setCity(dto.getCity());
            newUser.setStreet(dto.getStreet());
            newUser.setPostalCode(dto.getPostalCode());
            newUser.getRoles().add(Role.USER);

            User savedUser = userRepository.save(newUser);

            log.info("Registered user: {} " + savedUser.getEmail());

            return savedUser;

        } catch (UserAlreadyExistException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while registering user: {} "+  ex.getMessage());
            throw new RuntimeException("An error occurred during registration.");
        }
    }

}
