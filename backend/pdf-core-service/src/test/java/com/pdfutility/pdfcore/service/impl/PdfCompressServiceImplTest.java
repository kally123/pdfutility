package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.pdfcore.model.CompressionLevel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PdfCompressServiceImpl}.
 * Tests the core compression logic with sample PDFs.
 */
class PdfCompressServiceImplTest {

    private PdfCompressServiceImpl service;

    @BeforeEach
    void setUp() {
        // The synchronous compress path does not use jobRepository or storageClient
        service = new PdfCompressServiceImpl(
                Mockito.mock(com.pdfutility.pdfcore.repository.PdfJobRepository.class),
                Mockito.mock(com.pdfutility.pdfcore.service.StorageClient.class)
        );
    }

    @Test
    void compressPdf_shouldReturnValidPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Compression test");

        StepVerifier.create(service.compressPdf(input, CompressionLevel.MEDIUM, false, false))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.remaining()).isGreaterThan(0);
                    assertValidPdf(result);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(CompressionLevel.class)
    void compressPdf_allCompressionLevels(CompressionLevel level) throws IOException {
        ByteBuffer input = PdfTestHelper.createSamplePdf("Level test: " + level);

        StepVerifier.create(service.compressPdf(input.duplicate(), level, false, false))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.remaining()).isGreaterThan(0);
                    assertValidPdf(result);
                })
                .verifyComplete();
    }

    @Test
    void compressPdf_withRemoveMetadata_shouldReturnValidPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createPdfWithMetadata("My Title", "Author Name");

        StepVerifier.create(service.compressPdf(input, CompressionLevel.HIGH, true, false))
                .assertNext(result -> {
                    assertValidPdf(result);
                    // Verify metadata was removed
                    try {
                        byte[] bytes = toBytes(result);
                        PDDocument doc = Loader.loadPDF(bytes);
                        assertThat(doc.getDocumentInformation().getTitle()).isNull();
                        assertThat(doc.getDocumentInformation().getAuthor()).isNull();
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void compressPdf_withOptimizeImages_shouldReturnValidPdf() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(2);

        StepVerifier.create(service.compressPdf(input, CompressionLevel.MEDIUM, false, true))
                .assertNext(result -> {
                    assertThat(result.remaining()).isGreaterThan(0);
                    assertValidPdf(result);
                })
                .verifyComplete();
    }

    @Test
    void compressPdf_multiPageDocument_shouldPreservePageCount() throws IOException {
        ByteBuffer input = PdfTestHelper.createMultiPagePdf(5);

        StepVerifier.create(service.compressPdf(input, CompressionLevel.LOW, false, false))
                .assertNext(result -> {
                    try {
                        byte[] bytes = toBytes(result);
                        PDDocument doc = Loader.loadPDF(bytes);
                        assertThat(doc.getNumberOfPages()).isEqualTo(5);
                        doc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void assertValidPdf(ByteBuffer buffer) {
        try {
            byte[] bytes = toBytes(buffer);
            PDDocument doc = Loader.loadPDF(bytes);
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
            doc.close();
        } catch (IOException e) {
            throw new AssertionError("Result is not a valid PDF", e);
        }
    }

    private byte[] toBytes(ByteBuffer buffer) {
        buffer = buffer.duplicate();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
