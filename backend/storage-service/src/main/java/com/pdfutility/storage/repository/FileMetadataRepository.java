package com.pdfutility.storage.repository;

import com.pdfutility.storage.model.FileMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * File Metadata Repository - R2DBC (Reactive).
 */
@Repository
public interface FileMetadataRepository extends ReactiveCrudRepository<FileMetadata, String> {

    @Query("SELECT * FROM files WHERE user_id = :userId ORDER BY created_at DESC LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<FileMetadata> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Mono<Long> countByUserId(String userId);

    @Query("SELECT * FROM files WHERE is_temporary = true AND expires_at < :now")
    Flux<FileMetadata> findExpiredTemporaryFiles(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM files WHERE id = :id")
    Mono<Integer> deleteFileById(String id);

    @Query("SELECT COALESCE(SUM(size_bytes), 0) FROM files WHERE user_id = :userId")
    Mono<Long> getTotalStorageUsedByUser(String userId);
}
