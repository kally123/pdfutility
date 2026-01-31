package com.pdfutility.storage.service.impl;

import com.pdfutility.common.exception.StorageException;
import com.pdfutility.storage.service.StorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Local file system storage provider.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(@Value("${storage.local.base-path:./uploads}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath();
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(basePath);
            log.info("Local storage initialized at: {}", basePath);
        } catch (IOException e) {
            throw new StorageException("Failed to initialize local storage", e);
        }
    }

    @Override
    public Mono<String> store(String path, ByteBuffer content, String contentType) {
        return Mono.fromCallable(() -> {
            Path fullPath = basePath.resolve(path);
            Files.createDirectories(fullPath.getParent());
            
            byte[] bytes = new byte[content.remaining()];
            content.get(bytes);
            
            Files.write(fullPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("File stored at: {}", fullPath);
            
            return path;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorMap(e -> new StorageException("Failed to store file: " + path, e));
    }

    @Override
    public Mono<ByteBuffer> retrieve(String path) {
        return Mono.fromCallable(() -> {
            Path fullPath = basePath.resolve(path);
            if (!Files.exists(fullPath)) {
                throw new StorageException("File not found: " + path);
            }
            
            byte[] bytes = Files.readAllBytes(fullPath);
            return ByteBuffer.wrap(bytes);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorMap(e -> {
            if (e instanceof StorageException) return e;
            return new StorageException("Failed to retrieve file: " + path, e);
        });
    }

    @Override
    public Mono<Void> delete(String path) {
        return Mono.fromRunnable(() -> {
            try {
                Path fullPath = basePath.resolve(path);
                Files.deleteIfExists(fullPath);
                log.debug("File deleted: {}", fullPath);
            } catch (IOException e) {
                throw new StorageException("Failed to delete file: " + path, e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    @Override
    public Mono<Boolean> exists(String path) {
        return Mono.fromCallable(() -> Files.exists(basePath.resolve(path)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getProviderName() {
        return "local";
    }
}
