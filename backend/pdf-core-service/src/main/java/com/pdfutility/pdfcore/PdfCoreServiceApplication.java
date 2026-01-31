package com.pdfutility.pdfcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    public static void main(String[] args) {
        SpringApplication.run(PdfCoreServiceApplication.class, args);
    }
}
