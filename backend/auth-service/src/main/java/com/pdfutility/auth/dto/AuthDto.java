package com.pdfutility.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * Authentication request/response DTOs - Immutable.
 */
public final class AuthDto {

    private AuthDto() {}

    @Value
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email;

        @NotBlank(message = "Password is required")
        String password;

        Boolean rememberMe;
    }

    @Value
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password;

        @NotBlank(message = "First name is required")
        String firstName;

        String lastName;
    }

    @Value
    @Builder
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        String refreshToken;
    }

    @Value
    @Builder
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email;
    }

    @Value
    @Builder
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        String token;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword;
    }

    @Value
    @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword;
    }

    @Value
    @Builder
    public static class AuthResponse {
        String accessToken;
        String refreshToken;
        Long expiresIn;
        String tokenType;
        UserResponse user;
    }

    @Value
    @Builder
    public static class UserResponse {
        String id;
        String email;
        String firstName;
        String lastName;
        String fullName;
        String avatarUrl;
        Boolean emailVerified;
        java.util.List<String> roles;
    }

    @Value
    @Builder
    public static class TokenRefreshResponse {
        String accessToken;
        String refreshToken;
        Long expiresIn;
    }
}
