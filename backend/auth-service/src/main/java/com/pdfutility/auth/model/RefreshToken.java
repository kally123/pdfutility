package com.pdfutility.auth.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token entity.
 */
@Table("refresh_tokens")
@Builder
public record RefreshToken(
        @Id
        String id,

        @Column("user_id")
        String userId,

        @Column("token_hash")
        String tokenHash,

        @Column("expires_at")
        LocalDateTime expiresAt,

        @Column("revoked")
        Boolean revoked,

        @Column("created_at")
        LocalDateTime createdAt
) {
    public static RefreshToken create(String userId, String tokenHash, long expirationMs) {
        return RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }
}
