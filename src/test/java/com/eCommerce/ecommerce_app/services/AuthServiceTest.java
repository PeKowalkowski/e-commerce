package com.eCommerce.ecommerce_app.services;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.exceptions.UserAlreadyExistException;
import com.eCommerce.ecommerce_app.requests.LoginRequestDto;
import com.eCommerce.ecommerce_app.requests.RegistrationRequestDto;
import com.eCommerce.ecommerce_app.responses.LoginResponseDto;
import com.eCommerce.ecommerce_app.respositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegistrationRequestDto validDto;
    private LoginRequestDto loginDto;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validDto = new RegistrationRequestDto();
        validDto.setEmail("test@example.com");
        validDto.setFirstName("Jan");
        validDto.setLastName("Kowalski");
        validDto.setUsername("jkowalski");
        validDto.setPassword("password123");
        validDto.setPhoneNumber("123456789");
        validDto.setCountry("Poland");
        validDto.setCity("Warsaw");
        validDto.setStreet("Main Street");
        validDto.setPostalCode("00-001");


        loginDto = new LoginRequestDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("correctPassword");

        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPassword("encodedPassword");
        existingUser.getRoles().add(Role.USER);
    }

    //Registration
    @Test
    void registerUser_ShouldRegisterSuccessfully_WhenEmailNotExists() {
        // given
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User registeredUser = authService.registerUser(validDto);

        // then
        assertNotNull(registeredUser);
        assertEquals(validDto.getEmail(), registeredUser.getEmail());
        assertEquals(validDto.getFirstName(), registeredUser.getFirstName());
        assertEquals(validDto.getLastName(), registeredUser.getLastName());
        assertEquals(validDto.getUsername(), registeredUser.getUsername());
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertTrue(registeredUser.getRoles().contains(Role.USER));
        verify(userRepository).existsByEmail(validDto.getEmail());
        verify(passwordEncoder).encode(validDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowUserAlreadyExistException_WhenEmailExists() {
        // given
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(true);

        // when + then
        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
                () -> authService.registerUser(validDto));
        assertEquals("A user with this email already exists", exception.getMessage());

        verify(userRepository).existsByEmail(validDto.getEmail());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registerUser_ShouldThrowRuntimeException_WhenSaveFails() {
        // given
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        // when + then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.registerUser(validDto));
        assertEquals("An error occurred during registration.", exception.getMessage());

        verify(userRepository).existsByEmail(validDto.getEmail());
        verify(passwordEncoder).encode(validDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldAssignOnlyUserRole() {
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerUser(validDto);

        assertEquals(1, registeredUser.getRoles().size());
        assertTrue(registeredUser.getRoles().contains(Role.USER));
    }

    @Test
    void registerUser_ShouldWork_WhenOptionalFieldsAreNull() {
        validDto.setStreet(null);
        validDto.setPhoneNumber(null);

        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerUser(validDto);

        assertNull(registeredUser.getStreet());
        assertEquals(validDto.getEmail(), registeredUser.getEmail());
    }

    @Test
    void registerUser_ShouldEncodePassword_BeforeSavingToDatabase() {
        // given
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // then
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertNotEquals(validDto.getPassword(), savedUser.getPassword(),
                "The user password should be encrypted before saving to the database");
        assertEquals("encodedPassword123", savedUser.getPassword(),
                "The encrypted password should be set to the user");
    }

    //Login
    @Test
    void login_ShouldReturnTokenAndSuccessMessage_WhenCredentialsAreCorrect() {
        when(userRepository.findByUsername(loginDto.getUsername()))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginDto.getPassword(), existingUser.getPassword()))
                .thenReturn(true);

        LoginResponseDto response = authService.login(loginDto);

        assertNotNull(response);
        assertNotNull(response.getToken(), "Token should not be null");
        assertEquals("Login successful", response.getMessage());

        verify(userRepository).findByUsername(loginDto.getUsername());
        verify(passwordEncoder).matches(loginDto.getPassword(), existingUser.getPassword());
    }

    @Test
    void login_ShouldReturnNullTokenAndErrorMessage_WhenUserDoesNotExist() {
        when(userRepository.findByUsername(loginDto.getUsername()))
                .thenReturn(Optional.empty());

        LoginResponseDto response = authService.login(loginDto);

        assertNotNull(response);
        assertNull(response.getToken(), "Token should be null");
        assertEquals("Invalid username or password", response.getMessage());

        verify(userRepository).findByUsername(loginDto.getUsername());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_ShouldReturnNullTokenAndErrorMessage_WhenPasswordIsIncorrect() {
        when(userRepository.findByUsername(loginDto.getUsername()))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginDto.getPassword(), existingUser.getPassword()))
                .thenReturn(false);

        LoginResponseDto response = authService.login(loginDto);

        assertNotNull(response);
        assertNull(response.getToken(), "Token should be null");
        assertEquals("Invalid username or password", response.getMessage());

        verify(userRepository).findByUsername(loginDto.getUsername());
        verify(passwordEncoder).matches(loginDto.getPassword(), existingUser.getPassword());
    }


}
