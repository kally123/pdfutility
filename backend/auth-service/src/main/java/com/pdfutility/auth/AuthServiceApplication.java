package com.pdfutility.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authentication Service - Main Application
 * 
 * Provides:
 * - User registration and authentication
 * - JWT token generation and validation
 * - OAuth 2.0 integration (Google, Microsoft)
 * - Password reset functionality
 * - Role-based access control
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
