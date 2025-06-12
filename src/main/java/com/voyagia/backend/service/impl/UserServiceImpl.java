package com.voyagia.backend.service.impl;

import com.voyagia.backend.entity.User;
import com.voyagia.backend.entity.UserRole;
import com.voyagia.backend.exception.InvalidUserDataException;
import com.voyagia.backend.exception.UserAlreadyExistsException;
import com.voyagia.backend.exception.UserNotFoundException;
import com.voyagia.backend.repository.UserRepository;
import com.voyagia.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // default readonly
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // Constructor 주입을 위한 final fields
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection method
     *
     * @param userRepository  user data access
     * @param passwordEncoder password encode
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // 쓰기 작업이므로 트랜잭션 적용
    public User registerUser(User user) {
        logger.info("Register user try: email={}, username={}", user.getEmail(), user.getUsername());

        // validate data
        validateUserForRegistration(user);

        // Check duplication
        if (existsByEmail((user.getEmail()))) {
            logger.warn("Email already exists: {}", user.getEmail());
            throw new UserAlreadyExistsException("email", user.getEmail());
        }

        if (existsByUsername(user.getUsername())) {
            logger.warn("Username already exists: {}", user.getUsername());
            throw new UserAlreadyExistsException("username", user.getUsername());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }
        user.setIsActive(true);

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    @Override
    public User findById(Long id) {
        logger.debug("Find user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Optional<User> findByIdOptional(Long id) {
        logger.debug("Find user by ID (Optional): {}", id);
        return userRepository.findById(id);
    }

    @Override
    public User findByEmail(String email) {
        logger.debug("Find user by Email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email, "email"));
    }

    @Override
    public Optional<User> findByEmailOptional(String email) {
        logger.debug("Find user by Email (Optional) : {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByUsername(String username) {
        logger.debug("Find user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username, "username"));
    }

    @Override
    public Optional<User> findByUsernameOptional(String username) {
        logger.debug("Find user by username (Optional): {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAllActiveUsers() {
        logger.debug("Find all active users");
        return userRepository.findByIsActiveTrue();
    }

    @Override
    public Page<User> findAllWithPagination(Pageable pageable) {
        logger.debug("Find all users (paginated): page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> findActiveUsersWithPagination(Pageable pageable) {
        logger.debug("Find all active users (paginated): page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        return userRepository.findByIsActiveTrue(pageable);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        logger.info("Update user detail: id={}", id);

        User existingUser = findById(id);

        // Check duplication
        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
                existsByEmail(userDetails.getEmail())) {
            throw new UserAlreadyExistsException("email", userDetails.getEmail());
        }

        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
                existsByUsername(userDetails.getUsername())) {
            throw new UserAlreadyExistsException("username", userDetails.getUsername());
        }

        // Update only certain fields
        if (StringUtils.hasText(userDetails.getEmail())) {
            existingUser.setEmail(userDetails.getEmail());
        }
        if (StringUtils.hasText(userDetails.getUsername())) {
            existingUser.setUsername(userDetails.getUsername());
        }
        if (StringUtils.hasText(userDetails.getFirstName())) {
            existingUser.setFirstName(userDetails.getFirstName());
        }
        if (StringUtils.hasText(userDetails.getLastName())) {
            existingUser.setLastName(userDetails.getLastName());
        }
        if (StringUtils.hasText(userDetails.getPhone())) {
            existingUser.setPhone(userDetails.getPhone());
        }
        if (StringUtils.hasText(userDetails.getAddress())) {
            existingUser.setAddress(userDetails.getAddress());
        }

        User updatedUser = userRepository.save(existingUser);
        logger.info("User detail updated successfully: id={}", updatedUser.getId());

        return updatedUser;
    }

    @Override
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        logger.info("Change password: userId={}", id);

        User user = findById(id);

        // Current password check
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Invalid password: userId={}", id);
            throw new InvalidUserDataException("Invalid current password.");
        }

        // Validate new password
        validatePassword(newPassword);

        // Encode new password and save
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully: userId={}", id);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        logger.info("Deactivate user: id={}", id);

        User user = findById(id);
        user.setIsActive(false);
        userRepository.save(user);

        logger.info("User deactivated successfully: id={}", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        logger.info("Activate user: id={}", id);

        User user = findById(id);
        user.setIsActive(true);
        userRepository.save(user);

        logger.info("User activated successfully: id={}", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        logger.debug("Find user with keyword: keyword={}", keyword);

        if (!StringUtils.hasText(keyword)) {
            return findAllActiveUsers();
        }

        return userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        keyword, keyword, keyword, keyword);
    }

    @Override
    public Page<User> searchUsersWithPagination(String keyword, Pageable pageable) {
        logger.debug("Find user with keyword (paginated): keyword={}, page={}, size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        if (!StringUtils.hasText(keyword)) {
            return findActiveUsersWithPagination(pageable);
        }

        return userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        keyword, keyword, keyword, keyword, pageable);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }

    /**
     * Validate user data for register
     *
     * @param user user data
     * @throws InvalidUserDataException invalid data
     */
    private void validateUserForRegistration(User user) {
        if (user == null) {
            throw new InvalidUserDataException("User data required.");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new InvalidUserDataException("Email is required.");
        }

        if (!StringUtils.hasText(user.getUsername())) {
            throw new InvalidUserDataException("Username is required.");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new InvalidUserDataException("Password is required.");
        }

        if (!StringUtils.hasText(user.getFirstName())) {
            throw new InvalidUserDataException("First name is required.");
        }

        if (!StringUtils.hasText(user.getLastName())) {
            throw new InvalidUserDataException("Last name is required.");
        }

        validatePassword(user.getPassword());
    }

    /**
     * Validate password
     *
     * @param password password
     * @throws InvalidUserDataException invalid password
     */
    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new InvalidUserDataException("Password is required.");
        }

        if (password.length() > 128) {
            throw new InvalidUserDataException("Password cannot exceed 128 characters.");
        }

        if (password.length() < 6) {
            throw new InvalidUserDataException("Password must be at least 6 characters long.");
        }

        // Requires at least one uppercase letter
        // if (!password.matches(".*[A-Z].*")) {
        // throw new InvalidUserDataException("Password must contain at least one
        // uppercase letter.");
        // }

        // Requires at least one lowercase letter
        // if (!password.matches(".*[a-z].*")) {
        // throw new InvalidUserDataException("Password must contain at least one
        // lowercase letter.");
        // }

        // Requires at least one digit
        // if (!password.matches(".*[0-9].*")) {
        // throw new InvalidUserDataException("Password must contain at least one
        // number.");
        // }

        // Requires at least one special character
        // if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
        // throw new InvalidUserDataException("Password must contain at least one
        // special character.");
        // }

        // Prevent consecutive identical characters (ex: aaa, 111)
        // if (password.matches(".*(.)\\1{2,}.*")) {
        // throw new InvalidUserDataException("Password cannot contain more than 2
        // consecutive identical characters.");
        // }

        // Prevent common keyboard patterns (ex: qwerty, 123456)
        // String[] commonPatterns = {"123456", "qwerty", "abcdef", "password"};
        // for (String pattern : commonPatterns) {
        // if (password.toLowerCase().contains(pattern)) {
        // throw new InvalidUserDataException("Password cannot contain common
        // patterns.");
        // }
        // }

    }
}
