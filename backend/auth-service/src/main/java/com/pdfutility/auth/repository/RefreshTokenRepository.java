package com.pdfutility.auth.repository;

import com.pdfutility.auth.model.RefreshToken;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Refresh Token Repository - R2DBC (Reactive).
 */
@Repository
public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, String> {

    @Query("SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND revoked = false AND expires_at > NOW()")
    Mono<RefreshToken> findValidToken(String tokenHash);

    Flux<RefreshToken> findByUserIdAndRevokedFalse(String userId);

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked = true WHERE id = :id")
    Mono<Integer> revokeToken(String id);

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId")
    Mono<Integer> revokeAllUserTokens(String userId);

    @Modifying
    @Query("DELETE FROM refresh_tokens WHERE expires_at < :threshold OR revoked = true")
    Mono<Integer> deleteExpiredTokens(LocalDateTime threshold);
}
