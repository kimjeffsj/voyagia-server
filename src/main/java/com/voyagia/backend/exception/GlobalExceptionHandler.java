package com.voyagia.backend.exception;

import com.voyagia.backend.dto.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * Centralized exception handling for the entire application
 * Provides consistent error responses and logging
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================================
    // Category Exceptions
    // ================================

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCategoryNotFoundException(CategoryNotFoundException e) {
        logger.warn("Category not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Category not found: " + e.getMessage()));
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException e) {
        logger.warn("Category already exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Category already exists: " + e.getMessage()));
    }

    @ExceptionHandler(InvalidCategoryDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCategoryDataException(InvalidCategoryDataException e) {
        logger.warn("Invalid category data: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid category data: " + e.getMessage()));
    }

    @ExceptionHandler(CircularReferenceException.class)
    public ResponseEntity<ApiResponse<String>> handleCircularReferenceException(CircularReferenceException e) {
        logger.warn("Circular reference detected: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Circular reference: " + e.getMessage()));
    }

    @ExceptionHandler(CategoryDeleteException.class)
    public ResponseEntity<ApiResponse<String>> handleCategoryDeleteException(CategoryDeleteException e) {
        logger.warn("Category cannot be deleted: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Category cannot be deleted: " + e.getMessage()));
    }

    // ================================
    // Product Exceptions
    // ================================

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleProductNotFoundException(ProductNotFoundException e) {
        logger.warn("Product not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Product not found: " + e.getMessage()));
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleProductAlreadyExistsException(ProductAlreadyExistsException e) {
        logger.warn("Product already exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Product already exists: " + e.getMessage()));
    }

    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidProductDataException(InvalidProductDataException e) {
        logger.warn("Invalid product data: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid product data: " + e.getMessage()));
    }

    // ================================
    // User Exceptions
    // ================================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(UserNotFoundException e) {
        logger.warn("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found: " + e.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        logger.warn("User already exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("User already exists: " + e.getMessage()));
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidUserDataException(InvalidUserDataException e) {
        logger.warn("Invalid user data: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid user data: " + e.getMessage()));
    }

    // ================================
    // Cart & Order Exceptions
    // ================================

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCartNotFoundException(CartNotFoundException e) {
        logger.warn("Cart not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Cart not found: " + e.getMessage()));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCartItemNotFoundException(CartItemNotFoundException e) {
        logger.warn("Cart item not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Cart item not found: " + e.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleOrderNotFoundException(OrderNotFoundException e) {
        logger.warn("Order not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Order not found: " + e.getMessage()));
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidOrderException(InvalidOrderException e) {
        logger.warn("Invalid order: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid order: " + e.getMessage()));
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidQuantityException(InvalidQuantityException e) {
        logger.warn("Invalid quantity: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid quantity: " + e.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficientStockException(InsufficientStockException e) {
        logger.warn("Insufficient stock: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Insufficient stock: " + e.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentFailedException(PaymentFailedException e) {
        logger.error("Payment failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.error("Payment failed: " + e.getMessage()));
    }

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleOrderProcessingException(OrderProcessingException e) {
        logger.error("Order processing error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Order processing error: " + e.getMessage()));
    }

    // ================================
    // Spring Framework Exceptions
    // ================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation failed: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("Invalid request body: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid request body format"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.warn("Invalid parameter type: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid parameter type: " + e.getName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("HTTP method not supported: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("HTTP method not supported: " + e.getMethod()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("No handler found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found: " + e.getRequestURL()));
    }

    // ================================
    // Database Exceptions
    // ================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("Data integrity violation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Data integrity constraint violation"));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<String>> handleSQLException(SQLException e) {
        logger.error("Database error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Database operation failed"));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleEntityNotFoundException(EntityNotFoundException e) {
        logger.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Entity not found"));
    }

    // ================================
    // Generic Exceptions
    // ================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid argument: " + e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        logger.error("Runtime exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}