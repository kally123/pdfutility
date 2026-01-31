package com.pdfutility.common.event;

/**
 * PDF operation event types following naming convention:
 * {domain}.{action}.{version}
 */
public final class PdfEventTypes {

    private PdfEventTypes() {
        // Utility class
    }

    // PDF Merge Events
    public static final String PDF_MERGE_REQUESTED = "pdf.merge.requested.v1";
    public static final String PDF_MERGE_STARTED = "pdf.merge.started.v1";
    public static final String PDF_MERGE_COMPLETED = "pdf.merge.completed.v1";
    public static final String PDF_MERGE_FAILED = "pdf.merge.failed.v1";

    // PDF Compress Events
    public static final String PDF_COMPRESS_REQUESTED = "pdf.compress.requested.v1";
    public static final String PDF_COMPRESS_STARTED = "pdf.compress.started.v1";
    public static final String PDF_COMPRESS_COMPLETED = "pdf.compress.completed.v1";
    public static final String PDF_COMPRESS_FAILED = "pdf.compress.failed.v1";

    // PDF Edit Events
    public static final String PDF_EDIT_REQUESTED = "pdf.edit.requested.v1";
    public static final String PDF_EDIT_STARTED = "pdf.edit.started.v1";
    public static final String PDF_EDIT_COMPLETED = "pdf.edit.completed.v1";
    public static final String PDF_EDIT_FAILED = "pdf.edit.failed.v1";

    // PDF Split Events
    public static final String PDF_SPLIT_REQUESTED = "pdf.split.requested.v1";
    public static final String PDF_SPLIT_COMPLETED = "pdf.split.completed.v1";
    public static final String PDF_SPLIT_FAILED = "pdf.split.failed.v1";

    // PDF Convert Events
    public static final String PDF_CONVERT_REQUESTED = "pdf.convert.requested.v1";
    public static final String PDF_CONVERT_COMPLETED = "pdf.convert.completed.v1";
    public static final String PDF_CONVERT_FAILED = "pdf.convert.failed.v1";

    // File Events
    public static final String FILE_UPLOADED = "file.uploaded.v1";
    public static final String FILE_DELETED = "file.deleted.v1";
    public static final String FILE_DOWNLOAD_REQUESTED = "file.download.requested.v1";

    // User Events
    public static final String USER_REGISTERED = "user.registered.v1";
    public static final String USER_LOGGED_IN = "user.logged.in.v1";
    public static final String USER_LOGGED_OUT = "user.logged.out.v1";
}
