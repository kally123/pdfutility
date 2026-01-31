package com.pdfutility.pdfcore;

import io.projectreactor.tools.blockhound.BlockHound;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * PDF Core Service - Main Application
 * 
 * Provides reactive PDF processing capabilities:
 * - Merge multiple PDFs
 * - Compress PDFs
 * - Edit PDFs (add text, images, watermarks)
 * - Split PDFs
 * - Convert PDFs
 */
@SpringBootApplication
public class PdfCoreServiceApplication {

    static {
        // Install BlockHound in development to detect blocking calls
        if (isDevProfile()) {
            BlockHound.install(
                BlockHound.builder()
                    .allowBlockingCallsInside("java.util.UUID", "randomUUID")
                    .allowBlockingCallsInside("org.apache.pdfbox", "load")
                    .build()
            );
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PdfCoreServiceApplication.class, args);
    }

    private static boolean isDevProfile() {
        String profiles = System.getProperty("spring.profiles.active", "");
        return Arrays.asList(profiles.split(",")).contains("dev");
    }
}
