package com.voyagia.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Change password request DTO
 * <p>
 * 사용자가 비밀번호를 변경할때 사용되는 데이터 전송 객체
 * 현재 비밀번호 확인과 새 비밀번호 설정이 포함
 */
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 128, message = "New password must be between 6 and 128 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmNewPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmNewPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    // Validation methods

    public boolean isNewPasswordConfirmed() {
        if (newPassword == null || confirmNewPassword == null) {
            return false;
        }
        return newPassword.equals(confirmNewPassword);
    }


    public boolean isPasswordChanged() {
        if (currentPassword == null || newPassword == null) {
            return false;
        }
        return !currentPassword.equals(newPassword);
    }

    public String validatePasswordRelations() {
        if (!isNewPasswordConfirmed()) {
            return "New password and confirm password do not match";
        }
        
        if (!isPasswordChanged()) {
            return "New password must be different from current password";
        }

        return null;
    }

    /**
     * 검증 실패 시 구체적인 에러 메시지들을 반환
     *
     * @return 에러 메시지 리스트
     */
    public java.util.List<String> getValidationErrors() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            errors.add("Current password is required");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            errors.add("New password is required");
        }

        if (confirmNewPassword == null || confirmNewPassword.trim().isEmpty()) {
            errors.add("Password confirmation is required");
        }

        if (!isNewPasswordConfirmed()) {
            errors.add("New password and confirm password do not match");
        }

        if (!isPasswordChanged()) {
            errors.add("New password must be different from current password");
        }

        if (newPassword != null) {
            if (newPassword.length() < 6) {
                errors.add("New password must be at least 6 characters long");
            }
            if (newPassword.length() > 128) {
                errors.add("New password cannot exceed 128 characters");
            }
        }

        return errors;
    }

    // Getters/Setters

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
