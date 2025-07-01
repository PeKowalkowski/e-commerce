package com.eCommerce.ecommerce_app.exceptions;

import com.eCommerce.ecommerce_app.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;

import org.slf4j.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExist(UserAlreadyExistException ex, HttpServletRequest request) {
        ProblemDetail problem = createProblemDetail(
                HttpStatus.CONFLICT,
                "User Already Exists",
                "user-already-exists",
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
        log.info("Unexpected error occurred" + ex);
        ProblemDetail problem = createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error",
                "internal-error",
                "An unexpected error occurred. Please try again later.",
                request
        );
        problem.setProperty("debugMessage", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }


    private ProblemDetail createProblemDetail(HttpStatus status, String title, String type, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(type));

        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("errorCode", type);
        problem.setProperty("messageForUser", detail);

        return problem;
    }
}
