package com.pdfutility.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Service - Main Application
 * 
 * Provides:
 * - Request routing to microservices
 * - Rate limiting
 * - JWT authentication
 * - Circuit breaker patterns
 * - Load balancing
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
