package com.voyagia.backend.repository;

import com.voyagia.backend.entity.User;
import com.voyagia.backend.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user methods

    /**
     * Find by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    // Find user active ot not

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find all active users (paginated)
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Find all deactivated users
     */
    List<User> findByIsActiveFalse();

    /**
     * Find all deactivated users (paginated)
     */
    Page<User> findByIsActiveFalse(Pageable pageable);

    /**
     * Count Active users
     */
    long countByIsActiveTrue();

    /**
     * Count deactivated users
     */
    long countByIsActiveFalse();

    // Find users by role

    /**
     * Find user by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find user by role (paginated)
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Find user by role && active
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Find user by role && active (paginated)
     */
    Page<User> findByRoleAndIsActiveTrue(User role, Pageable pageable);

    // Find user by keyword

    /**
     * First name, last name, email, username keyword search
     * Ignore upper or lower case
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String firstName, String lastName, String email, String username);

    /**
     * First name, last name, email, username keyword search (paginated)
     * Ignore upper or lower case
     */
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String firstName, String lastName, String email, String username, Pageable pageable);


    // Search query

    /**
     * Query user (uses JPQL)
     * among first name, last name, email, username
     *
     * @param searchTerm search term
     * @param pageable   paging information
     * @return paginated result
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);


    /**
     * Query user among active users
     *
     * @param searchTerm search term
     * @param pageable   paging information
     * @return paginated result
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Query user by roles
     *
     * @param searchTerm search term
     * @param role       user role
     * @param pageable   paging information
     * @return paginated result
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersByRole(@Param("searchTerm") String searchTerm,
                                 @Param("role") UserRole role,
                                 Pageable pageable);

    /**
     * Query user with specific email domain
     *
     * @param domain email domain (ex: "gmail.com")
     * @return List of users with specific domain
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * Query New users
     *
     * @param pageable paging information
     * @return recently registered users
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    Page<User> findRecentUsers(Pageable pageable);
}
