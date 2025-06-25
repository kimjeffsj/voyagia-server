package com.voyagia.backend.controller;

import com.voyagia.backend.dto.auth.JwtAuthenticationResponse;
import com.voyagia.backend.dto.common.ApiResponse;
import com.voyagia.backend.dto.user.UserDTOMapper;
import com.voyagia.backend.dto.user.UserLoginRequest;
import com.voyagia.backend.dto.user.UserRegistrationRequest;
import com.voyagia.backend.dto.user.UserResponse;
import com.voyagia.backend.entity.User;
import com.voyagia.backend.exception.InvalidUserDataException;
import com.voyagia.backend.exception.UserNotFoundException;
import com.voyagia.backend.security.JwtUtil;
import com.voyagia.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Auth Controller
 * <p>
 * 사용자 인증 관련 REST API
 * Login, Signup, token validation
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public AuthController(UserService userService, UserDTOMapper userDTOMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDTOMapper = userDTOMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * User Signup
     *
     * @param request       signup request DTO
     * @param bindingResult validation
     * @return result and JWT token
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody UserRegistrationRequest request,
            BindingResult bindingResult
    ) {

        logger.info("User signup attempt: email={}, username={}",
                request.getEmail(), request.getUsername());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors)
            );
        }

        if (!request.isPasswordConfirmed()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Password do not match")
            );
        }

        try {
            User user = userDTOMapper.toEntity(request);

            User savedUser = userService.registerUser(user);

            String jwt = jwtUtil.generateToken(savedUser);

            UserResponse userResponse = userDTOMapper.toResponse(savedUser);
            JwtAuthenticationResponse authResponse = new JwtAuthenticationResponse(
                    jwt, jwtUtil.getExpirationInSeconds(), userResponse
            );

            logger.info("User sign up successful: id={}, email={}",
                    savedUser.getId(), savedUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("Signup successfully", authResponse)
            );
        } catch (Exception e) {
            logger.error("User sign up failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Sign up failed: " + e.getMessage())
            );
        }
    }

    /**
     * Login
     *
     * @param request       login request DTO
     * @param bindingResult validation
     * @return JWT token and user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest request,
            BindingResult bindingResult
    ) {
        logger.info("User login attempt: {}", request.getUsernameOrEmail());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors)
            );
        }

        try {
            User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());

            if (!user.getIsActive()) {
                logger.warn("Login attempt for deactivated user: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Deactivated user, contact us")
                );
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Invalid password for user: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("Wrong email/username or password")
                );
            }

            String jwt = jwtUtil.generateToken(user);

            UserResponse userResponse = userDTOMapper.toResponse(user);
            JwtAuthenticationResponse authResponse = new JwtAuthenticationResponse(
                    jwt, jwtUtil.getExpirationInSeconds(), userResponse
            );

            logger.info("User login successful: id={}, email={}",
                    user.getId(), user.getEmail());

            return ResponseEntity.ok(
                    ApiResponse.success("Login successful", authResponse)
            );
        } catch (UserNotFoundException e) {
            logger.warn("Login failed - user not found: {}", request.getUsernameOrEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Wrong email/username or password")
            );
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error occurred during login")
            );
        }
    }

    /**
     * Validate JWT token
     *
     * @param token Bearer token
     * @return validity & user
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(
            @RequestHeader("Authorization") String token) {
        logger.debug("Token validation request");

        try {
            String jwt = jwtUtil.extractToken(token);

            String username = jwtUtil.getUsernameFromToken(jwt);
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            User user = userService.findById(userId);

            if (!jwtUtil.validateToken(jwt, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("Invalid token")
                );
            }

            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Inactive user")
                );
            }

            UserResponse userResponse = userDTOMapper.toResponse(user);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("valid", true);
            responseData.put("user", userResponse);
            responseData.put("expiresIn", jwtUtil.getExpirationInSeconds());

            return ResponseEntity.ok(
                    ApiResponse.success("Valid token", responseData)
            );
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Token validation failed")
            );
        }
    }

    /**
     * Refresh token
     *
     * @param token Bearer token
     * @return new JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestHeader("Authorization") String token) {
        logger.debug("Token refresh request");

        try {
            String jwt = jwtUtil.extractToken(token);

            Long userId = jwtUtil.getUserIdFromToken(jwt);

            User user = userService.findById(userId);

            String username = jwtUtil.getUsernameFromToken(jwt);
            if (!user.getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("Invalid token")
                );
            }

            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Inactive user")
                );
            }

            String newJwt = jwtUtil.generateToken(user);

            UserResponse userResponse = userDTOMapper.toResponse(user);
            JwtAuthenticationResponse authResponse = new JwtAuthenticationResponse(
                    newJwt, jwtUtil.getExpirationInSeconds(), userResponse
            );

            logger.info("Token refreshed successfully for user: {}", user.getUsername());

            return ResponseEntity.ok(
                    ApiResponse.success("Token refreshed successfully", authResponse)
            );

        } catch (Exception e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Failed to refresh token")
            );
        }
    }

    /**
     * Logout (delete token from client)
     *
     * @param token Bearer token
     * @return Logout message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String token
    ) {
        logger.info("User logout request");

        try {
            String jwt = jwtUtil.extractToken(token);
            String username = jwtUtil.getUsernameFromToken(jwt);

            logger.info("User logout successful: {}", username);

            return ResponseEntity.ok(
                    ApiResponse.success("Logout successful")
            );
        } catch (Exception e) {
            logger.debug("Logout request with invalid token: {}", e.getMessage());
            return ResponseEntity.ok(
                    ApiResponse.success("Logout successful")
            );
        }
    }


    /**
     * Get current user
     *
     * @param token Bearer token
     * @return user
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader("Authorization") String token
    ) {
        logger.debug("Get current user request");

        try {
            String jwt = jwtUtil.extractToken(token);

            Long userId = jwtUtil.getUserIdFromToken(jwt);

            User user = userService.findById(userId);

            if (!jwtUtil.validateToken(jwt, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("Invalid token")
                );
            }


            UserResponse userResponse = userDTOMapper.toResponse(user);

            return ResponseEntity.ok(
                    ApiResponse.success("User information retrieved successfully", userResponse)
            );
        } catch (Exception e) {
            logger.warn("Get current user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Failed to retrieve user information")
            );
        }
    }

    // Utility methods

    /**
     * Find user by username or email
     *
     * @param usernameOrEmail username or email
     * @return user
     * @throws UserNotFoundException user not found
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail.contains("@")) {
            Optional<User> userOptional = userService.findByEmailOptional(usernameOrEmail);
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        } else {
            Optional<User> userOptional = userService.findByUsernameOptional(usernameOrEmail);
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }

        throw new UserNotFoundException(usernameOrEmail, "username or email");
    }

    // Exceptions

    /**
     * Handle InvalidUserDataException
     */
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidUserException(
            InvalidUserDataException e) {
        logger.warn("Invalid user data: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
        );
    }


    /**
     * Handle UserNotFoundException
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(
            UserNotFoundException e) {
        logger.warn("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Unable to find user")
        );
    }

    /**
     * Handle General Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error in AuthController: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Internal server error occurred")
        );
    }
}
