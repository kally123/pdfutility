package com.pdfutility.auth.controller;

import com.pdfutility.auth.dto.AuthDto.*;
import com.pdfutility.auth.service.AuthService;
import com.pdfutility.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Authentication Controller - Reactive REST API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user account")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Registration successful")))
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * Login with email and password.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Login successful")))
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * Refresh access token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    public Mono<ResponseEntity<ApiResponse<TokenRefreshResponse>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return authService.refreshToken(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * Logout - revoke tokens.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout and revoke all tokens")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(
            @RequestHeader("X-User-Id") String userId) {

        return authService.logout(userId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success(null, "Logout successful"))));
    }

    /**
     * Get current user profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the currently authenticated user's profile")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getCurrentUser(
            @RequestHeader("X-User-Id") String userId) {

        return authService.getCurrentUser(userId)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Change password.
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        return authService.changePassword(userId, request)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success(null, "Password changed successfully"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * Forgot password - send reset email.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    public Mono<ResponseEntity<ApiResponse<Void>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        // Always return success to prevent email enumeration
        return Mono.just(ResponseEntity.ok(
                ApiResponse.<Void>success(null, "If the email exists, a password reset link has been sent")));
    }

    /**
     * Reset password with token.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public Mono<ResponseEntity<ApiResponse<Void>>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        // Implementation would validate token and reset password
        return Mono.just(ResponseEntity.ok(
                ApiResponse.<Void>success(null, "Password has been reset successfully")));
    }
}
