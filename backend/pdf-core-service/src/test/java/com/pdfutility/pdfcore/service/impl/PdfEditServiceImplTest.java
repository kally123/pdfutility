package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.pdfcore.dto.PdfOperationResponses.PdfInfoResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PdfEditServiceImpl}.
 * Tests editing operations: add text, watermark, rotate, extract, protect/unlock.
 */
class PdfEditServiceImplTest {

    private PdfEditServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PdfEditServiceImpl();
    }

    // ── addText ────────────────────────────────────────────────────────────────

    @Test
    void addText_shouldReturnValidPdfWithSamePage() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Original text");

        StepVerifier.create(service.addText(input, "Hello World", 1, 100, 500, 12, "Helvetica", "#000000"))
                .assertNext(result -> {
                    assertThat(result.remaining()).isGreaterThan(0);
                    assertValidPdf(result, 1);
                })
                .verifyComplete();
    }

    @Test
    void addText_invalidPageNumber_shouldFail() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("One page");

        StepVerifier.create(service.addText(input, "Text", 99, 100, 500, 12, "Helvetica", "#000000"))
                .expectError(com.pdfutility.common.exception.PdfProcessingException.class)
                .verify();
    }

    @Test
    void addText_withColorHex_shouldReturnValidPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Color test");

        StepVerifier.create(service.addText(input, "Red text", 1, 50, 600, 14, "Helvetica", "#FF0000"))
                .assertNext(result -> assertValidPdf(result, 1))
                .verifyComplete();
    }

    @Test
    void addText_withNullColor_shouldDefaultToBlack() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Null color test");

        StepVerifier.create(service.addText(input, "Default color", 1, 50, 600, 14, "Helvetica", null))
                .assertNext(result -> assertValidPdf(result, 1))
                .verifyComplete();
    }

    // ── addWatermark ───────────────────────────────────────────────────────────

    @Test
    void addWatermark_shouldReturnValidPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Watermark target");

        StepVerifier.create(service.addWatermark(input, "CONFIDENTIAL", 0.3f, 45))
                .assertNext(result -> assertValidPdf(result, 1))
                .verifyComplete();
    }

    @Test
    void addWatermark_multiPage_shouldWatermarkAllPages() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(3);

        StepVerifier.create(service.addWatermark(input, "DRAFT", 0.2f, 0))
                .assertNext(result -> assertValidPdf(result, 3))
                .verifyComplete();
    }

    // ── rotatePages ────────────────────────────────────────────────────────────

    @Test
    void rotatePages_allPages_shouldApplyRotation() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(2);

        StepVerifier.create(service.rotatePages(input, 90, null))
                .assertNext(result -> {
                    assertValidPdf(result, 2);
                    try {
                        PDDocument doc = Loader.loadPDF(toBytes(result));
                        doc.getPages().forEach(page ->
                                assertThat(page.getRotation() % 360).isEqualTo(90));
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void rotatePages_specificPages_shouldOnlyRotateSpecified() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(3);

        StepVerifier.create(service.rotatePages(input, 180, List.of(2)))
                .assertNext(result -> {
                    assertValidPdf(result, 3);
                    try {
                        PDDocument doc = Loader.loadPDF(toBytes(result));
                        assertThat(doc.getPage(0).getRotation()).isEqualTo(0);   // page 1 unchanged
                        assertThat(doc.getPage(1).getRotation()).isEqualTo(180); // page 2 rotated
                        assertThat(doc.getPage(2).getRotation()).isEqualTo(0);   // page 3 unchanged
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    // ── extractPages ───────────────────────────────────────────────────────────

    @Test
    void extractPages_validRange_shouldReturnSubset() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(5);

        StepVerifier.create(service.extractPages(input, 2, 4))
                .assertNext(result -> assertValidPdf(result, 3))
                .verifyComplete();
    }

    @Test
    void extractPages_singlePage_shouldReturnOnePagePdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(4);

        StepVerifier.create(service.extractPages(input, 3, 3))
                .assertNext(result -> assertValidPdf(result, 1))
                .verifyComplete();
    }

    @Test
    void extractPages_invalidRange_shouldFail() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(3);

        StepVerifier.create(service.extractPages(input, 5, 10))
                .expectError(com.pdfutility.common.exception.PdfProcessingException.class)
                .verify();
    }

    // ── getPdfInfo ─────────────────────────────────────────────────────────────

    @Test
    void getPdfInfo_shouldReturnCorrectPageCount() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(4);

        StepVerifier.create(service.getPdfInfo(input))
                .assertNext(info -> {
                    assertThat(info.getPageCount()).isEqualTo(4);
                    assertThat(info.getIsEncrypted()).isFalse();
                    assertThat(info.getDimensions()).isNotNull();
                    assertThat(info.getDimensions().getWidth()).isGreaterThan(0);
                    assertThat(info.getDimensions().getHeight()).isGreaterThan(0);
                    assertThat(info.getFileSizeBytes()).isGreaterThan(0);
                })
                .verifyComplete();
    }

    @Test
    void getPdfInfo_withMetadata_shouldReturnMetadata() throws IOException {
        ByteBuffer input = PdfTestHelper.createPdfWithMetadata("Test Title", "Test Author");

        StepVerifier.create(service.getPdfInfo(input))
                .assertNext((PdfInfoResponse info) -> {
                    assertThat(info.getTitle()).isEqualTo("Test Title");
                    assertThat(info.getAuthor()).isEqualTo("Test Author");
                })
                .verifyComplete();
    }

    // ── protectPdf / unlockPdf ─────────────────────────────────────────────────

    @Test
    void protectPdf_shouldReturnEncryptedPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Secret content");

        StepVerifier.create(service.protectPdf(input, "userPass", "ownerPass", true, false))
                .assertNext(result -> {
                    assertThat(result.remaining()).isGreaterThan(0);
                    try {
                        PDDocument doc = Loader.loadPDF(toBytes(result), "userPass");
                        assertThat(doc.isEncrypted()).isTrue();
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void unlockPdf_correctPassword_shouldReturnUnencryptedPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Protected content");

        // First protect, then unlock
        service.protectPdf(input, "pass123", "ownerPass", false, false)
                .flatMap(protectedPdf -> service.unlockPdf(protectedPdf, "pass123"))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    try {
                        PDDocument doc = Loader.loadPDF(toBytes(result));
                        assertThat(doc.isEncrypted()).isFalse();
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void unlockPdf_wrongPassword_shouldFail() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Protected content");

        service.protectPdf(input, "correctPass", "ownerPass", false, false)
                .flatMap(protectedPdf -> service.unlockPdf(protectedPdf, "wrongPass"))
                .as(StepVerifier::create)
                .expectError()
                .verify();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void assertValidPdf(ByteBuffer buffer, int expectedPages) {
        try {
            PDDocument doc = Loader.loadPDF(toBytes(buffer));
            assertThat(doc.getNumberOfPages()).isEqualTo(expectedPages);
            doc.close();
        } catch (IOException e) {
            throw new AssertionError("Result is not a valid PDF with " + expectedPages + " pages", e);
        }
    }

    private byte[] toBytes(ByteBuffer buffer) {
        buffer = buffer.duplicate();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
