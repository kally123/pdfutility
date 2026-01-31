package com.pdfutility.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Storage Service - Main Application
 * 
 * Provides:
 * - File upload/download
 * - Azure Blob Storage integration
 * - S3-compatible storage (MinIO)
 * - Temporary file management
 * - File metadata storage
 */
@SpringBootApplication
public class StorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}
