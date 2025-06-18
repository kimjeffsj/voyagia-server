package com.voyagia.backend.dto.user;

import com.voyagia.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * User DTO Mapper class
 * <p>
 * Entity와 DTO 간의 변환을 담당하는 유틸리티 클래스
 */
@Component
public class UserDTOMapper {

    /**
     * UserRegistrationRequest DTO to User Entity
     *
     * @param request Sign up request DTO
     * @return User Entity
     */
    public User toEntity(UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        return user;
    }

    /**
     * UserUpdateRequest DTO to Entity
     *
     * @param user    original user entity
     * @param request update request DTO
     * @return updated User entity
     */
    public User updateEntity(User user, UserUpdateRequest request) {
        if (user == null || request == null) {
            return user;
        }

        // update where not null
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        return user;
    }

    /**
     * User Entity to UserResponse DTO
     *
     * @param user user entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(user);
    }

    /**
     * User Entity to UserResponse DTO list
     *
     * @param users user entity
     * @return UserResponse DTO list
     */
    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create simplified UserResponse DTO
     * (For profile list etc.)
     *
     * @param user User Entity
     * @return simple UserResponse DTO
     */
    public UserResponse toSimpleResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());

        return response;
    }

    /**
     * User Entity to simple UserResponse DTO list
     *
     * @param users User Entity list
     * @return simple UserResponse DTO list
     */
    public List<UserResponse> toSimpleResponseList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Detailed UserResponse for admins
     * (include all information)
     *
     * @param user User Entity
     * @return detailed UserResponse DTO
     */
    public UserResponse toDetailedResponse(User user) {
        return toResponse(user);
    }
}
