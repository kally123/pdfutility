package com.pdfutility.pdfcore.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Utility class for creating sample PDF bytes used in unit tests.
 */
public final class PdfTestHelper {

    private PdfTestHelper() {
    }

    /**
     * Creates a minimal single-page PDF with a text label.
     */
    public static ByteBuffer createSamplePdf(String pageText) throws IOException {
        try (PDDocument document = new PDDocument()) {
            addPage(document, pageText);
            return toByteBuffer(document);
        }
    }

    /**
     * Creates a multi-page PDF.
     */
    public static ByteBuffer createMultiPagePdf(int pageCount) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 1; i <= pageCount; i++) {
                addPage(document, "Page " + i);
            }
            return toByteBuffer(document);
        }
    }

    /**
     * Creates a PDF with metadata set.
     */
    public static ByteBuffer createPdfWithMetadata(String title, String author) throws IOException {
        try (PDDocument document = new PDDocument()) {
            addPage(document, "Metadata test");
            document.getDocumentInformation().setTitle(title);
            document.getDocumentInformation().setAuthor(author);
            return toByteBuffer(document);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static void addPage(PDDocument document, String text) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            cs.newLineAtOffset(50, 700);
            cs.showText(text);
            cs.endText();
        }
    }

    private static ByteBuffer toByteBuffer(PDDocument document) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        return ByteBuffer.wrap(out.toByteArray());
    }
}
