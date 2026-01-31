package com.pdfutility.pdfcore.service;

import com.pdfutility.pdfcore.dto.PdfOperationResponses.PdfInfoResponse;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * PDF Edit Service Interface - Reactive.
 * Provides editing operations like adding text, images, watermarks.
 */
public interface PdfEditService {

    /**
     * Add text to a PDF.
     *
     * @param fileContent PDF file content
     * @param text Text to add
     * @param pageNumber Page number (1-indexed)
     * @param x X coordinate
     * @param y Y coordinate
     * @param fontSize Font size
     * @param fontName Font name
     * @param color Text color (hex)
     * @return Modified PDF content
     */
    Mono<ByteBuffer> addText(ByteBuffer fileContent, String text, int pageNumber,
                              float x, float y, int fontSize, String fontName, String color);

    /**
     * Add watermark to PDF.
     *
     * @param fileContent PDF file content
     * @param watermarkText Watermark text
     * @param opacity Opacity (0.0 - 1.0)
     * @param rotation Rotation angle
     * @return Modified PDF content
     */
    Mono<ByteBuffer> addWatermark(ByteBuffer fileContent, String watermarkText,
                                   float opacity, int rotation);

    /**
     * Add image watermark to PDF.
     *
     * @param fileContent PDF file content
     * @param imageContent Image content
     * @param opacity Opacity (0.0 - 1.0)
     * @param position Position on page
     * @return Modified PDF content
     */
    Mono<ByteBuffer> addImageWatermark(ByteBuffer fileContent, ByteBuffer imageContent,
                                        float opacity, String position);

    /**
     * Rotate PDF pages.
     *
     * @param fileContent PDF file content
     * @param angle Rotation angle (90, 180, 270)
     * @param pageNumbers Page numbers to rotate (null = all)
     * @return Modified PDF content
     */
    Mono<ByteBuffer> rotatePages(ByteBuffer fileContent, int angle, List<Integer> pageNumbers);

    /**
     * Split PDF into multiple files.
     *
     * @param fileContent PDF file content
     * @param fromPage Start page (1-indexed)
     * @param toPage End page (inclusive)
     * @return Extracted PDF content
     */
    Mono<ByteBuffer> extractPages(ByteBuffer fileContent, int fromPage, int toPage);

    /**
     * Get PDF information/metadata.
     *
     * @param fileContent PDF file content
     * @return PDF information
     */
    Mono<PdfInfoResponse> getPdfInfo(ByteBuffer fileContent);

    /**
     * Protect PDF with password.
     *
     * @param fileContent PDF file content
     * @param userPassword User password
     * @param ownerPassword Owner password
     * @param allowPrinting Allow printing
     * @param allowCopying Allow copying
     * @return Protected PDF content
     */
    Mono<ByteBuffer> protectPdf(ByteBuffer fileContent, String userPassword,
                                 String ownerPassword, boolean allowPrinting, boolean allowCopying);

    /**
     * Remove password protection from PDF.
     *
     * @param fileContent PDF file content
     * @param password Password to unlock
     * @return Unlocked PDF content
     */
    Mono<ByteBuffer> unlockPdf(ByteBuffer fileContent, String password);
}
