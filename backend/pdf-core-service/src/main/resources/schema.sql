-- PDF Jobs Table
CREATE TABLE IF NOT EXISTS pdf_jobs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    input_file_ids TEXT[] NOT NULL,
    output_file_id VARCHAR(36),
    parameters JSONB,
    error_message TEXT,
    progress INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- Create index for user queries
CREATE INDEX IF NOT EXISTS idx_pdf_jobs_user_id ON pdf_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_pdf_jobs_status ON pdf_jobs(status);
CREATE INDEX IF NOT EXISTS idx_pdf_jobs_created_at ON pdf_jobs(created_at DESC);

-- PDF Job Status Enum Values
-- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

-- PDF Job Type Enum Values
-- MERGE, SPLIT, COMPRESS, EDIT, CONVERT, ROTATE, PROTECT, UNLOCK, OCR, WATERMARK
