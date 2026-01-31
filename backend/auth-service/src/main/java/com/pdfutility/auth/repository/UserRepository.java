package com.pdfutility.auth.repository;

import com.pdfutility.auth.model.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * User Repository - R2DBC (Reactive).
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    @Modifying
    @Query("UPDATE users SET email_verified = true, updated_at = :now WHERE id = :userId")
    Mono<Integer> verifyEmail(String userId, LocalDateTime now);

    @Modifying
    @Query("UPDATE users SET password_hash = :passwordHash, updated_at = :now WHERE id = :userId")
    Mono<Integer> updatePassword(String userId, String passwordHash, LocalDateTime now);

    @Modifying
    @Query("UPDATE users SET account_locked = false, failed_login_attempts = 0, updated_at = :now WHERE id = :userId")
    Mono<Integer> unlockAccount(String userId, LocalDateTime now);

    @Modifying
    @Query("UPDATE users SET last_login_at = :now, failed_login_attempts = 0, updated_at = :now WHERE id = :userId")
    Mono<Integer> recordSuccessfulLogin(String userId, LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE users 
        SET failed_login_attempts = failed_login_attempts + 1,
            account_locked = CASE WHEN failed_login_attempts >= 4 THEN true ELSE account_locked END,
            updated_at = :now 
        WHERE id = :userId
        """)
    Mono<Integer> recordFailedLogin(String userId, LocalDateTime now);
}
