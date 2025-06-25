package com.voyagia.backend.controller;

import com.voyagia.backend.dto.user.*;
import com.voyagia.backend.entity.User;
import com.voyagia.backend.exception.InvalidUserDataException;
import com.voyagia.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Controller
 * <p>
 * Sign up, sign in, profile management, search user
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;

    public UserController(UserService userService, UserDTOMapper userDTOMapper) {
        this.userService = userService;
        this.userDTOMapper = userDTOMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegistrationRequest request,
            BindingResult bindingResult) {
        logger.info("User registration attempt: email={}, username={}",
                request.getEmail(), request.getUsername());

        // 1. Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        if (!request.isPasswordConfirmed()) {
            Map<String, String> error = new HashMap<>();
            error.put("password", "Password and confirm password do not match");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            User user = userDTOMapper.toEntity(request);

            User savedUser = userService.registerUser(user);

            UserResponse response = userDTOMapper.toResponse(savedUser);

            logger.info("User registered successfully: id={}, email={}",
                    savedUser.getId(), savedUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("User registration failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Check duplicate email
     *
     * @param email email
     * @return available or not
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @RequestParam String email) {
        logger.debug("Check email availability: {}", email);

        boolean exists = userService.existsByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    /**
     * Check duplicate username
     *
     * @param username username
     * @return available or not
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsernameAvailability(
            @RequestParam String username) {
        logger.debug("Check username availability: {}", username);

        boolean exists = userService.existsByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("username", username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all users (paginated)
     *
     * @param page      page number(start from 0)
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @return user list (paginated)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.debug("Get all users: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        // Sort
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Query
        Page<User> userPage = userService.findAllWithPagination(pageable);

        // Response
        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOMapper.toResponseList(userPage.getContent()));
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("pageSize", userPage.getSize());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * Get active users (paginated)
     *
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @return active user list (paginated)
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        logger.debug("Get active users: page={}, size={}", page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<User> userPage = userService.findActiveUsersWithPagination(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOMapper.toResponseList(userPage.getContent()));
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("pageSize", userPage.getSize());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * search user
     *
     * @param keyword   search keyword
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @return user list
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        logger.debug("Search users: keyword={}, page={}, size={}", keyword, page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<User> userPage = userService.searchUsersWithPagination(keyword, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOMapper.toResponseList(userPage.getContent()));
        response.put("keyword", keyword);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("pageSize", userPage.getSize());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * Advanced user search
     *
     * @param searchRequest search request DTO
     * @param bindingResult validation result
     * @return user list
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<?> advancedSearchUsers(
            @Valid @RequestBody UserSearchRequest searchRequest,
            BindingResult bindingResult) {

        logger.debug("Advanced search users: {}", searchRequest);

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Sort.Direction sortDirection = searchRequest.isDescending() ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                    searchRequest.getPage(),
                    searchRequest.getSize(),
                    Sort.by(sortDirection, searchRequest.getSortBy())
            );

            Page<User> userPage;
            if (searchRequest.hasKeyword()) {
                userPage = userService.searchUsersWithPagination(searchRequest.getKeyword(), pageable);
            } else {
                // if no keyword query all
                if (searchRequest.hasActiveFilter() && searchRequest.getIsActive()) {
                    userPage = userService.findActiveUsersWithPagination(pageable);
                } else {
                    userPage = userService.findAllWithPagination(pageable);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", userDTOMapper.toResponseList(userPage.getContent()));
            response.put("searchCriteria", searchRequest);
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("pageSize", userPage.getSize());
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Advanced search failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error occurred while searching: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update user
     *
     * @param id            user ID
     * @param request       update request DTO
     * @param bindingResult Validation
     * @return Updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Update user: id={}", id);

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));

            return ResponseEntity.badRequest().body(errors);
        }

        // Check update field
        if (!request.hasAnyUpdateField()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "No updated field");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            User updateUser = new User();
            userDTOMapper.updateEntity(updateUser, request);

            User updatedUser = userService.updateUser(id, updateUser);

            UserResponse response = userDTOMapper.toResponse(updatedUser);

            logger.info("User updated successfully: id={}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("User update failed: id={}, error={}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Change password
     *
     * @param id            user ID
     * @param request       change password request DTO
     * @param bindingResult validation
     * @return success/fail message
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Change password: userId={}", id);

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        List<String> validationErrors = request.getValidationErrors();
        if (!validationErrors.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Change password request is not valid");
            error.put("errors", validationErrors);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            logger.info("Password changed successfully: userId={}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Password change failed: userId={}, error={}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Deactivate user
     *
     * @param id user ID
     * @return message
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long id) {
        logger.info("Deactivate user: id={}", id);

        userService.deactivateUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deactivated successfully");

        logger.info("User deactivated successfully: id={}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * @param id user ID
     * @return message
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long id) {
        logger.info("Activate user: id={}", id);

        userService.activateUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User activated successfully");

        logger.info("User activated successfully: id={}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * User stats
     *
     * @return user stats (number of total users, active users, inactive users, and active percentage)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        logger.debug("Get user statistics");

        long totalUsers = userService.countAllUsers();
        long activeUsers = userService.countActiveUsers();
        long inactiveUsers = totalUsers - activeUsers;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("activePercentage", totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0);

        return ResponseEntity.ok(stats);
    }

    /**
     * handle InvalidUserDataException
     */
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<Map<String, String>> handleInvalidUserDateException(
            InvalidUserDataException e) {
        logger.warn("Invalid user data: {}", e.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * handle General Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error in UserController: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("message", "Internal server error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
