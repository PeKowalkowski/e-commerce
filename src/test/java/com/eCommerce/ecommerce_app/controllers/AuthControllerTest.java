package com.eCommerce.ecommerce_app.controllers;

import com.eCommerce.ecommerce_app.entities.User;
import com.eCommerce.ecommerce_app.enums.Role;
import com.eCommerce.ecommerce_app.exceptions.UserAlreadyExistException;
import com.eCommerce.ecommerce_app.requests.LoginRequestDto;
import com.eCommerce.ecommerce_app.requests.RegistrationRequestDto;
import com.eCommerce.ecommerce_app.responses.LoginResponseDto;
import com.eCommerce.ecommerce_app.responses.RegistrationResponseDto;
import com.eCommerce.ecommerce_app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AuthController authController;

    private RegistrationRequestDto validDto;
    private User user;

    private LoginRequestDto validLoginDto;
    private LoginRequestDto invalidLoginDto;

    private LoginResponseDto successLoginResponse;
    private LoginResponseDto failureLoginResponse;

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

        user = new User();
        user.setId(1L);
        user.setEmail(validDto.getEmail());
        user.setFirstName(validDto.getFirstName());
        user.setLastName(validDto.getLastName());
        user.setUsername(validDto.getUsername());
        user.getRoles().add(Role.USER);

        validLoginDto = new LoginRequestDto();
        validLoginDto.setUsername("jkowalski");
        validLoginDto.setPassword("password123");

        invalidLoginDto = new LoginRequestDto();
        invalidLoginDto.setUsername("jkowalski");
        invalidLoginDto.setPassword("wrongPassword");

        successLoginResponse = new LoginResponseDto("valid-token-123", "Login successful");
        failureLoginResponse = new LoginResponseDto(null, "Invalid username or password");
    }

    //Registration
    @Test
    void register_ShouldReturnCreated_WhenValidInput() {
        // given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.registerUser(validDto)).thenReturn(user);

        // when
        var responseEntity = authController.register(validDto, bindingResult);
        RegistrationResponseDto responseBody = responseEntity.getBody();

        // then
        assertNotNull(responseBody);
        assertEquals(user.getId(), responseBody.getId());
        assertEquals(user.getUsername(), responseBody.getUsername());
        assertEquals(user.getEmail(), responseBody.getEmail());
        assertEquals("Registration successful", responseBody.getMessage());
        assertEquals(201, responseEntity.getStatusCodeValue());

        verify(authService).registerUser(validDto);
        verify(bindingResult).hasErrors();
    }

    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() {
        // given
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("field", "Invalid phone number")));

        // when
        var responseEntity = authController.register(validDto, bindingResult);
        RegistrationResponseDto responseBody = responseEntity.getBody();

        // then
        assertNotNull(responseBody);
        assertEquals("Validation failed", responseBody.getMessage());
        assertTrue(responseBody.getErrors().contains("Invalid phone number"));
        assertEquals(400, responseEntity.getStatusCodeValue());

        verify(authService, never()).registerUser(any());
        verify(bindingResult).hasErrors();
        verify(bindingResult).getAllErrors();
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        // given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.registerUser(validDto)).thenThrow(new UserAlreadyExistException("A user with this email already exists"));

        // when + then
        UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class,
                () -> authController.register(validDto, bindingResult));
        assertEquals("A user with this email already exists", ex.getMessage());

        verify(authService).registerUser(validDto);
        verify(bindingResult).hasErrors();
    }

    @Test
    void register_ShouldThrowRuntimeException_WhenUnexpectedError() {
        // given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.registerUser(validDto)).thenThrow(new RuntimeException("Unexpected error"));

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authController.register(validDto, bindingResult));
        assertEquals("Unexpected error", ex.getMessage());

        verify(authService).registerUser(validDto);
        verify(bindingResult).hasErrors();
    }

    @Test
    void register_ShouldReturnAllValidationErrors_WhenMultipleFieldsInvalid() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("phoneNumber", "Phone number too long"),
                new ObjectError("street", "Street cannot be empty")
        ));

        ResponseEntity<RegistrationResponseDto> response = authController.register(validDto, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertTrue(response.getBody().getErrors().contains("Phone number too long"));
        assertTrue(response.getBody().getErrors().contains("Street cannot be empty"));

        verify(authService, never()).registerUser(any());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenRequestBodyIsNull() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("requestBody", "Request body cannot be null")
        ));

        ResponseEntity<RegistrationResponseDto> response = authController.register(null, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getErrors().contains("Request body cannot be null"));

        verify(authService, never()).registerUser(any());
    }

    @Test
    void register_ShouldReturnError_WhenEmailInvalid() {
        validDto.setEmail("invalidEmailWithoutAt");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("email", "Email should be valid")
        ));

        ResponseEntity<RegistrationResponseDto> response = authController.register(validDto, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getErrors().contains("Email should be valid"));
        verify(authService, never()).registerUser(any());
    }

    @Test
    void register_ShouldReturnError_WhenPhoneNumberInvalid() {
        validDto.setPhoneNumber("123ABC");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("phoneNumber", "Phone number is invalid")
        ));

        ResponseEntity<RegistrationResponseDto> response = authController.register(validDto, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getErrors().contains("Phone number is invalid"));
        verify(authService, never()).registerUser(any());
    }

    @Test
    void register_ShouldReturnError_WhenUsernameTooLong() {
        validDto.setUsername("thisusernameisdefinitelywaytoolong");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("username", "Username must be at most 20 characters")
        ));

        ResponseEntity<RegistrationResponseDto> response = authController.register(validDto, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getErrors().contains("Username must be at most 20 characters"));
        verify(authService, never()).registerUser(any());
    }

    @Test
    void register_ShouldAllowOptionalFieldsToBeNull() {
        validDto.setPhoneNumber(null);
        validDto.setStreet(null);

        when(bindingResult.hasErrors()).thenReturn(false);
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(validDto.getUsername());
        mockUser.setEmail(validDto.getEmail());

        when(authService.registerUser(validDto)).thenReturn(mockUser);

        ResponseEntity<RegistrationResponseDto> response = authController.register(validDto, bindingResult);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Registration successful", response.getBody().getMessage());
        verify(authService).registerUser(validDto);
    }

    //Login
    @Test
    void login_ShouldReturn200AndToken_WhenCredentialsAreValid() {
        when(authService.login(validLoginDto)).thenReturn(successLoginResponse);

        ResponseEntity<LoginResponseDto> response = authController.login(validLoginDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("valid-token-123", response.getBody().getToken());
        assertEquals("Login successful", response.getBody().getMessage());

        verify(authService).login(validLoginDto);
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() {
        when(authService.login(invalidLoginDto)).thenReturn(failureLoginResponse);

        ResponseEntity<LoginResponseDto> response = authController.login(invalidLoginDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getToken());
        assertEquals("Invalid username or password", response.getBody().getMessage());

        verify(authService).login(invalidLoginDto);
    }

    //Logout
    @Test
    void logout_ShouldReturn200_WhenTokenIsValid() {
        String token = "valid-token";
        User user = new User();

        when(authService.getUserByToken(token)).thenReturn(user);
        doNothing().when(authService).logout(token);

        ResponseEntity<String> response = authController.logout(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());


        verify(authService).getUserByToken(token);
        verify(authService).logout(token);
    }
    @Test
    void logout_ShouldReturn401_WhenTokenIsMissing() {
        ResponseEntity<String> response = authController.logout(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized: token is missing or invalid", response.getBody());

        verify(authService, never()).getUserByToken(any());
        verify(authService, never()).logout(any());
    }
    @Test
    void logout_ShouldReturn401_WhenTokenIsInvalid() {
        String token = "invalid-token";

        when(authService.getUserByToken(token)).thenReturn(null);

        ResponseEntity<String> response = authController.logout(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized: token is missing or invalid", response.getBody());

        verify(authService).getUserByToken(token);
        verify(authService, never()).logout(any());
    }
}
