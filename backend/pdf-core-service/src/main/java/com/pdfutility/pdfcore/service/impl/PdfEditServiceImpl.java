package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.common.exception.PdfProcessingException;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.PdfDimensions;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.PdfInfoResponse;
import com.pdfutility.pdfcore.service.PdfEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

/**
 * PDF Edit Service Implementation.
 * Provides editing operations using PDFBox.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfEditServiceImpl implements PdfEditService {

    @Override
    public Mono<ByteBuffer> addText(ByteBuffer fileContent, String text, int pageNumber,
                                     float x, float y, int fontSize, String fontName, String color) {
        return Mono.fromCallable(() -> performAddText(fileContent, text, pageNumber, x, y, fontSize, fontName, color))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Added text to PDF page {}", pageNumber));
    }

    @Override
    public Mono<ByteBuffer> addWatermark(ByteBuffer fileContent, String watermarkText,
                                          float opacity, int rotation) {
        return Mono.fromCallable(() -> performAddWatermark(fileContent, watermarkText, opacity, rotation))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Added watermark to PDF"));
    }

    @Override
    public Mono<ByteBuffer> addImageWatermark(ByteBuffer fileContent, ByteBuffer imageContent,
                                               float opacity, String position) {
        return Mono.fromCallable(() -> performAddImageWatermark(fileContent, imageContent, opacity, position))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ByteBuffer> rotatePages(ByteBuffer fileContent, int angle, List<Integer> pageNumbers) {
        return Mono.fromCallable(() -> performRotatePages(fileContent, angle, pageNumbers))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Rotated PDF pages by {} degrees", angle));
    }

    @Override
    public Mono<ByteBuffer> extractPages(ByteBuffer fileContent, int fromPage, int toPage) {
        return Mono.fromCallable(() -> performExtractPages(fileContent, fromPage, toPage))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Extracted pages {}-{} from PDF", fromPage, toPage));
    }

    @Override
    public Mono<PdfInfoResponse> getPdfInfo(ByteBuffer fileContent) {
        return Mono.fromCallable(() -> extractPdfInfo(fileContent))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ByteBuffer> protectPdf(ByteBuffer fileContent, String userPassword,
                                        String ownerPassword, boolean allowPrinting, boolean allowCopying) {
        return Mono.fromCallable(() -> performProtect(fileContent, userPassword, ownerPassword, allowPrinting, allowCopying))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Protected PDF with password"));
    }

    @Override
    public Mono<ByteBuffer> unlockPdf(ByteBuffer fileContent, String password) {
        return Mono.fromCallable(() -> performUnlock(fileContent, password))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Unlocked protected PDF"));
    }

    // ========== Private Implementation Methods ==========

    private ByteBuffer performAddText(ByteBuffer fileContent, String text, int pageNumber,
                                       float x, float y, int fontSize, String fontName, String color) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                throw new PdfProcessingException("Invalid page number: " + pageNumber);
            }

            PDPage page = document.getPage(pageNumber - 1);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                contentStream.setNonStrokingColor(parseColor(color));
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(text);
                contentStream.endText();
            }

            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to add text to PDF: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performAddWatermark(ByteBuffer fileContent, String watermarkText,
                                            float opacity, int rotation) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            for (PDPage page : document.getPages()) {
                PDRectangle pageSize = page.getMediaBox();
                float pageWidth = pageSize.getWidth();
                float pageHeight = pageSize.getHeight();

                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                    // Set transparency
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setNonStrokingAlphaConstant(opacity);
                    contentStream.setGraphicsStateParameters(graphicsState);

                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 50);
                    contentStream.setNonStrokingColor(Color.LIGHT_GRAY);

                    // Center and rotate watermark
                    float x = pageWidth / 2;
                    float y = pageHeight / 2;
                    Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotation), x, y);
                    contentStream.setTextMatrix(matrix);
                    
                    // Adjust position for text centering
                    float textWidth = watermarkText.length() * 25; // Approximate
                    contentStream.newLineAtOffset(-textWidth / 2, 0);
                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }

            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to add watermark: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performAddImageWatermark(ByteBuffer fileContent, ByteBuffer imageContent,
                                                 float opacity, String position) {
        // Implementation for image watermark
        // Would use PDImageXObject to add image
        throw new UnsupportedOperationException("Image watermark not yet implemented");
    }

    private ByteBuffer performRotatePages(ByteBuffer fileContent, int angle, List<Integer> pageNumbers) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            int numPages = document.getNumberOfPages();

            for (int i = 0; i < numPages; i++) {
                // If pageNumbers is null, rotate all pages; otherwise only specified pages
                if (pageNumbers == null || pageNumbers.contains(i + 1)) {
                    PDPage page = document.getPage(i);
                    int currentRotation = page.getRotation();
                    page.setRotation((currentRotation + angle) % 360);
                }
            }

            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to rotate pages: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performExtractPages(ByteBuffer fileContent, int fromPage, int toPage) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument sourceDocument = Loader.loadPDF(inputBytes);
             PDDocument newDocument = new PDDocument()) {

            int numPages = sourceDocument.getNumberOfPages();
            if (fromPage < 1 || toPage > numPages || fromPage > toPage) {
                throw new PdfProcessingException(
                        String.format("Invalid page range: %d-%d (document has %d pages)", fromPage, toPage, numPages));
            }

            for (int i = fromPage - 1; i < toPage; i++) {
                PDPage page = sourceDocument.getPage(i);
                newDocument.addPage(page);
            }

            return saveDocument(newDocument);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract pages: " + e.getMessage(), e);
        }
    }

    private PdfInfoResponse extractPdfInfo(ByteBuffer fileContent) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            var info = document.getDocumentInformation();
            PDPage firstPage = document.getPage(0);
            PDRectangle mediaBox = firstPage.getMediaBox();

            return PdfInfoResponse.builder()
                    .pageCount(document.getNumberOfPages())
                    .author(info.getAuthor())
                    .title(info.getTitle())
                    .subject(info.getSubject())
                    .createdDate(toLocalDateTime(info.getCreationDate()))
                    .modifiedDate(toLocalDateTime(info.getModificationDate()))
                    .isEncrypted(document.isEncrypted())
                    .pdfVersion(String.valueOf(document.getVersion()))
                    .dimensions(PdfDimensions.builder()
                            .width(mediaBox.getWidth())
                            .height(mediaBox.getHeight())
                            .unit("points")
                            .build())
                    .fileSizeBytes((long) inputBytes.length)
                    .build();
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract PDF info: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performProtect(ByteBuffer fileContent, String userPassword,
                                       String ownerPassword, boolean allowPrinting, boolean allowCopying) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(allowPrinting);
            permissions.setCanExtractContent(allowCopying);
            permissions.setCanModify(false);
            permissions.setCanModifyAnnotations(false);

            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    ownerPassword != null ? ownerPassword : userPassword,
                    userPassword,
                    permissions);
            policy.setEncryptionKeyLength(256);

            document.protect(policy);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to protect PDF: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performUnlock(ByteBuffer fileContent, String password) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes, password)) {
            document.setAllSecurityToBeRemoved(true);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to unlock PDF: " + e.getMessage(), e);
        }
    }

    // ========== Utility Methods ==========

    private byte[] getBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private ByteBuffer saveDocument(PDDocument document) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    private Color parseColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) {
            return Color.BLACK;
        }
        try {
            return Color.decode(colorHex.startsWith("#") ? colorHex : "#" + colorHex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        if (calendar == null) return null;
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}

            PDPage firstPage = document.getPage(0);
            PDRectangle mediaBox = firstPage.getMediaBox();

            return PdfInfoResponse.builder()
                    .pageCount(document.getNumberOfPages())
                    .author(info.getAuthor())
                    .title(info.getTitle())
                    .subject(info.getSubject())
                    .createdDate(toLocalDateTime(info.getCreationDate()))
                    .modifiedDate(toLocalDateTime(info.getModificationDate()))
                    .isEncrypted(document.isEncrypted())
                    .pdfVersion(String.valueOf(document.getVersion()))
                    .dimensions(PdfDimensions.builder()
                            .width(mediaBox.getWidth())
                            .height(mediaBox.getHeight())
                            .unit("points")
                            .build())
                    .fileSizeBytes((long) inputBytes.length)
                    .build();
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract PDF info: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performProtect(ByteBuffer fileContent, String userPassword,
                                       String ownerPassword, boolean allowPrinting, boolean allowCopying) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(allowPrinting);
            permissions.setCanExtractContent(allowCopying);
            permissions.setCanModify(false);
            permissions.setCanModifyAnnotations(false);

            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    ownerPassword != null ? ownerPassword : userPassword,
                    userPassword,
                    permissions);
            policy.setEncryptionKeyLength(256);

            document.protect(policy);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to protect PDF: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performUnlock(ByteBuffer fileContent, String password) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes, password)) {
            document.setAllSecurityToBeRemoved(true);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to unlock PDF: " + e.getMessage(), e);
        }
    }

    // ========== Utility Methods ==========

    private byte[] getBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private ByteBuffer saveDocument(PDDocument document) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    private Color parseColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) {
            return Color.BLACK;
        }
        try {
            return Color.decode(colorHex.startsWith("#") ? colorHex : "#" + colorHex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        if (calendar == null) return null;
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}

            PDPage firstPage = document.getPage(0);
            PDRectangle mediaBox = firstPage.getMediaBox();

            return PdfInfoResponse.builder()
                    .pageCount(document.getNumberOfPages())
                    .author(info.getAuthor())
                    .title(info.getTitle())
                    .subject(info.getSubject())
                    .createdDate(toLocalDateTime(info.getCreationDate()))
                    .modifiedDate(toLocalDateTime(info.getModificationDate()))
                    .isEncrypted(document.isEncrypted())
                    .pdfVersion(String.valueOf(document.getVersion()))
                    .dimensions(PdfDimensions.builder()
                            .width(mediaBox.getWidth())
                            .height(mediaBox.getHeight())
                            .unit("points")
                            .build())
                    .fileSizeBytes((long) inputBytes.length)
                    .build();
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract PDF info: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performProtect(ByteBuffer fileContent, String userPassword,
                                       String ownerPassword, boolean allowPrinting, boolean allowCopying) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(allowPrinting);
            permissions.setCanExtractContent(allowCopying);
            permissions.setCanModify(false);
            permissions.setCanModifyAnnotations(false);

            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    ownerPassword != null ? ownerPassword : userPassword,
                    userPassword,
                    permissions);
            policy.setEncryptionKeyLength(256);

            document.protect(policy);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to protect PDF: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performUnlock(ByteBuffer fileContent, String password) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes, password)) {
            document.setAllSecurityToBeRemoved(true);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to unlock PDF: " + e.getMessage(), e);
        }
    }

    // ========== Utility Methods ==========

    private byte[] getBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private ByteBuffer saveDocument(PDDocument document) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    private Color parseColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) {
            return Color.BLACK;
        }
        try {
            return Color.decode(colorHex.startsWith("#") ? colorHex : "#" + colorHex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        if (calendar == null) return null;
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}

            PDPage firstPage = document.getPage(0);
            PDRectangle mediaBox = firstPage.getMediaBox();

            return PdfInfoResponse.builder()
                    .pageCount(document.getNumberOfPages())
                    .author(info.getAuthor())
                    .title(info.getTitle())
                    .subject(info.getSubject())
                    .createdDate(toLocalDateTime(info.getCreationDate()))
                    .modifiedDate(toLocalDateTime(info.getModificationDate()))
                    .isEncrypted(document.isEncrypted())
                    .pdfVersion(String.valueOf(document.getVersion()))
                    .dimensions(PdfDimensions.builder()
                            .width(mediaBox.getWidth())
                            .height(mediaBox.getHeight())
                            .unit("points")
                            .build())
                    .fileSizeBytes((long) inputBytes.length)
                    .build();
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract PDF info: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performProtect(ByteBuffer fileContent, String userPassword,
                                       String ownerPassword, boolean allowPrinting, boolean allowCopying) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(allowPrinting);
            permissions.setCanExtractContent(allowCopying);
            permissions.setCanModify(false);
            permissions.setCanModifyAnnotations(false);

            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    ownerPassword != null ? ownerPassword : userPassword,
                    userPassword,
                    permissions);
            policy.setEncryptionKeyLength(256);

            document.protect(policy);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to protect PDF: " + e.getMessage(), e);
        }
    }

    private ByteBuffer performUnlock(ByteBuffer fileContent, String password) {
        byte[] inputBytes = getBytes(fileContent);

        try (PDDocument document = Loader.loadPDF(inputBytes, password)) {
            document.setAllSecurityToBeRemoved(true);
            return saveDocument(document);
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to unlock PDF: " + e.getMessage(), e);
        }
    }

    // ========== Utility Methods ==========

    private byte[] getBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private ByteBuffer saveDocument(PDDocument document) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    private Color parseColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) {
            return Color.BLACK;
        }
        try {
            return Color.decode(colorHex.startsWith("#") ? colorHex : "#" + colorHex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        if (calendar == null) return null;
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}
