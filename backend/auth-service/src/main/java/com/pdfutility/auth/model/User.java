package com.pdfutility.auth.model;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity - R2DBC compatible.
 */
@Table("users")
@Builder
@With
public record User(
        @Id
        String id,

        @Column("email")
        String email,

        @Column("password_hash")
        String passwordHash,

        @Column("first_name")
        String firstName,

        @Column("last_name")
        String lastName,

        @Column("avatar_url")
        String avatarUrl,

        @Column("email_verified")
        Boolean emailVerified,

        @Column("account_locked")
        Boolean accountLocked,

        @Column("failed_login_attempts")
        Integer failedLoginAttempts,

        @Column("last_login_at")
        LocalDateTime lastLoginAt,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("updated_at")
        LocalDateTime updatedAt
) {
    public static User createNew(String email, String passwordHash, String firstName, String lastName) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email(email.toLowerCase())
                .passwordHash(passwordHash)
                .firstName(firstName)
                .lastName(lastName)
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public String fullName() {
        if (firstName == null && lastName == null) return email;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public User recordLoginSuccess() {
        return this.withFailedLoginAttempts(0)
                .withLastLoginAt(LocalDateTime.now())
                .withUpdatedAt(LocalDateTime.now());
    }

    public User recordLoginFailure() {
        int attempts = (failedLoginAttempts != null ? failedLoginAttempts : 0) + 1;
        User updated = this.withFailedLoginAttempts(attempts)
                .withUpdatedAt(LocalDateTime.now());
        
        // Lock account after 5 failed attempts
        if (attempts >= 5) {
            updated = updated.withAccountLocked(true);
        }
        return updated;
    }
}
