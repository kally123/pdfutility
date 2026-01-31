package com.pdfutility.auth.service;

import com.pdfutility.auth.dto.AuthDto.*;
import com.pdfutility.auth.model.RefreshToken;
import com.pdfutility.auth.model.User;
import com.pdfutility.auth.repository.RefreshTokenRepository;
import com.pdfutility.auth.repository.UserRepository;
import com.pdfutility.common.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Authentication Service - Reactive implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user.
     */
    public Mono<AuthResponse> register(RegisterRequest request) {
        return userRepository.existsByEmail(request.getEmail().toLowerCase())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new AuthenticationException("Email already registered"));
                    }

                    String passwordHash = passwordEncoder.encode(request.getPassword());
                    User user = User.createNew(
                            request.getEmail(),
                            passwordHash,
                            request.getFirstName(),
                            request.getLastName()
                    );

                    return userRepository.save(user);
                })
                .flatMap(this::generateAuthResponse)
                .doOnSuccess(response -> log.info("User registered: {}", request.getEmail()));
    }

    /**
     * Login with email and password.
     */
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail().toLowerCase())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid email or password")))
                .flatMap(user -> {
                    if (user.accountLocked()) {
                        return Mono.error(new AuthenticationException("Account is locked. Please contact support."));
                    }

                    if (!passwordEncoder.matches(request.getPassword(), user.passwordHash())) {
                        return userRepository.recordFailedLogin(user.id(), LocalDateTime.now())
                                .then(Mono.error(new AuthenticationException("Invalid email or password")));
                    }

                    return userRepository.recordSuccessfulLogin(user.id(), LocalDateTime.now())
                            .then(Mono.just(user));
                })
                .flatMap(this::generateAuthResponse)
                .doOnSuccess(response -> log.info("User logged in: {}", request.getEmail()));
    }

    /**
     * Refresh access token.
     */
    public Mono<TokenRefreshResponse> refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        return refreshTokenRepository.findValidToken(tokenHash)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid or expired refresh token")))
                .flatMap(refreshToken -> {
                    // Revoke old token
                    return refreshTokenRepository.revokeToken(refreshToken.id())
                            .then(userRepository.findById(refreshToken.userId()));
                })
                .flatMap(user -> {
                    List<String> roles = List.of("USER"); // Would fetch from user_roles table
                    String accessToken = jwtService.generateAccessToken(user.id(), user.email(), roles);
                    String newRefreshToken = jwtService.generateRefreshToken(user.id());
                    String newTokenHash = hashToken(newRefreshToken);

                    RefreshToken token = RefreshToken.create(user.id(), newTokenHash, jwtService.getRefreshTokenExpiration());

                    return refreshTokenRepository.save(token)
                            .map(saved -> TokenRefreshResponse.builder()
                                    .accessToken(accessToken)
                                    .refreshToken(newRefreshToken)
                                    .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                                    .build());
                });
    }

    /**
     * Logout - revoke all refresh tokens.
     */
    public Mono<Void> logout(String userId) {
        return refreshTokenRepository.revokeAllUserTokens(userId)
                .doOnSuccess(count -> log.info("Revoked {} tokens for user: {}", count, userId))
                .then();
    }

    /**
     * Change password.
     */
    public Mono<Void> changePassword(String userId, ChangePasswordRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.passwordHash())) {
                        return Mono.error(new AuthenticationException("Current password is incorrect"));
                    }

                    String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
                    return userRepository.updatePassword(userId, newPasswordHash, LocalDateTime.now());
                })
                .then(refreshTokenRepository.revokeAllUserTokens(userId))
                .then()
                .doOnSuccess(v -> log.info("Password changed for user: {}", userId));
    }

    /**
     * Get current user.
     */
    public Mono<UserResponse> getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .map(this::mapToUserResponse);
    }

    /**
     * Generate auth response with tokens.
     */
    private Mono<AuthResponse> generateAuthResponse(User user) {
        List<String> roles = List.of("USER"); // Would fetch from user_roles table
        String accessToken = jwtService.generateAccessToken(user.id(), user.email(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.id());
        String tokenHash = hashToken(refreshToken);

        RefreshToken token = RefreshToken.create(user.id(), tokenHash, jwtService.getRefreshTokenExpiration());

        return refreshTokenRepository.save(token)
                .map(saved -> AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                        .tokenType("Bearer")
                        .user(mapToUserResponse(user))
                        .build());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.id())
                .email(user.email())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .fullName(user.fullName())
                .avatarUrl(user.avatarUrl())
                .emailVerified(user.emailVerified())
                .roles(List.of("USER"))
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
