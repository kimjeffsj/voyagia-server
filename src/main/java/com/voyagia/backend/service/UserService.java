package com.voyagia.backend.service;


import com.voyagia.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Create new user
     *
     * @param user data
     * @return created user
     * @throws UserAlreadyExistsException email or username already exists
     * @throws InvalidUserDataException   invalid user data
     */
    User registerUser(User user);

    /**
     * Find user by ID
     *
     * @param id user ID
     * @return found user
     * @throws UserNotFoundException user not found
     */
    User findById(long id);

    /**
     * Find user by ID(return Optional)
     *
     * @param id user ID
     * @return found user(Optional)
     */
    Optional<User> findByIdOptional(Long id);

    /**
     * Find user by email
     *
     * @param email user email
     * @return found user
     * @throws UserNotFoundException user not found
     */
    User findByEmail(String email);

    /**
     * Find user by email (return Optional)
     *
     * @param email user email
     * @return found user (Optional)
     */
    Optional<User> findByEmailOptional(String email);

    /**
     * Find user by username
     *
     * @param username user's username
     * @return found user
     * @throws UserNotFoundException user not found
     */
    User findByUsername(String username);

    /**
     * Find user by username (return Optional)
     *
     * @param username user's username
     * @return found user
     * @throws UserNotFoundException user not found
     */
    Optional<User> findByUsernameOptional(String username);

    /**
     * Find All Active users
     *
     * @return active users list
     */
    List<User> findAllActiveUsers();

    /**
     * Find all users in paginated
     *
     * @param pageable page information
     * @return paginated all users list
     */
    Page<User> findAllWithPagination(Pageable pageable);

    /**
     * Find active users in paginated
     *
     * @param pageable paging information
     * @return paginated active users list
     */
    Page<User> findActiveUsersWithPagination(Pageable pageable);

    /**
     * Update user
     *
     * @param id          user ID
     * @param userDetails update user data
     * @return updated user
     * @throws UserNotFoundException      user not found
     * @throws UserAlreadyExistsException email/username already exists
     */
    User updateUser(Long id, User userDetails);

    /**
     * Change password
     *
     * @param id              user ID
     * @param currentPassword current password
     * @param newPassword     new password
     * @throws UserNotFoundException    user not found
     * @throws InvalidUserDataException wrong current password
     */
    void changePassword(Long id, String currentPassword, String newPassword);

    /**
     * Deactivate user
     *
     * @param id user ID
     * @throws UserNotFoundException user not found
     */
    void deactivateUser(Long id);

    /**
     * Activate user
     *
     * @param id user ID
     * @throws UserNotFoundException user not found
     */
    void activateUser(Long id);

    /**
     * Check if email exists
     *
     * @param email Email
     * @return true/false
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists
     *
     * @param username Useranme
     * @return true/false
     */
    boolean existsByUsername(String username);

    /**
     * Find users by keyword
     * Name, email, username based search
     *
     * @param keyword search keyword
     * @return found users list
     */
    List<User> searchUsers(String keyword);

    /**
     * Find users by keyword(paginated)
     *
     * @param keyword  search keyword
     * @param pageable paging information
     * @return paginated found users list
     */
    Page<User> searchUsersWithPagination(String keyword, Pageable pageable);

    /**
     * Count all users
     *
     * @return number of all users
     */
    long countAllUsers();

    /**
     * Count active users
     *
     * @return number of active users
     */
    long countActiveUsers();
}
