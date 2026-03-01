package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.pdfcore.repository.PdfJobRepository;
import com.pdfutility.pdfcore.service.StorageClient;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PdfMergeServiceImpl}.
 * Tests the core merge logic with sample PDFs.
 */
class PdfMergeServiceImplTest {

    private PdfMergeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PdfMergeServiceImpl(
                Mockito.mock(PdfJobRepository.class),
                Mockito.mock(StorageClient.class)
        );
    }

    @Test
    void mergePdfs_twoDocuments_shouldReturnSinglePdf() throws IOException {
        ByteBuffer pdf1 = PdfTestHelper.createSamplePdf("Document 1");
        ByteBuffer pdf2 = PdfTestHelper.createSamplePdf("Document 2");

        StepVerifier.create(service.mergePdfs(List.of(pdf1, pdf2), false))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.remaining()).isGreaterThan(0);
                    assertValidPdf(result, 2);
                })
                .verifyComplete();
    }

    @Test
    void mergePdfs_threeDocuments_shouldCombineAllPages() throws IOException {
        ByteBuffer pdf1 = PdfTestHelper.createMultiPagePdf(2);
        ByteBuffer pdf2 = PdfTestHelper.createMultiPagePdf(3);
        ByteBuffer pdf3 = PdfTestHelper.createSamplePdf("Single page");

        StepVerifier.create(service.mergePdfs(List.of(pdf1, pdf2, pdf3), false))
                .assertNext(result -> assertValidPdf(result, 6))
                .verifyComplete();
    }

    @Test
    void mergePdfs_singleDocument_shouldReturnEqualContent() throws IOException {
        ByteBuffer pdf = PdfTestHelper.createMultiPagePdf(3);

        StepVerifier.create(service.mergePdfs(List.of(pdf), false))
                .assertNext(result -> assertValidPdf(result, 3))
                .verifyComplete();
    }

    @Test
    void mergePdfs_withPreserveBookmarks_shouldReturnValidPdf() throws IOException {
        ByteBuffer pdf1 = PdfTestHelper.createSamplePdf("Bookmarked document A");
        ByteBuffer pdf2 = PdfTestHelper.createSamplePdf("Bookmarked document B");

        StepVerifier.create(service.mergePdfs(List.of(pdf1, pdf2), true))
                .assertNext(result -> assertValidPdf(result, 2))
                .verifyComplete();
    }

    @Test
    void mergePdfs_largeNumberOfDocuments_shouldSucceed() throws IOException {
        List<ByteBuffer> pdfs = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pdfs.add(PdfTestHelper.createSamplePdf("Document " + i));
        }

        StepVerifier.create(service.mergePdfs(pdfs, false))
                .assertNext(result -> assertValidPdf(result, 10))
                .verifyComplete();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void assertValidPdf(ByteBuffer buffer, int expectedPages) {
        try {
            byte[] bytes = toBytes(buffer);
            PDDocument doc = Loader.loadPDF(bytes);
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
