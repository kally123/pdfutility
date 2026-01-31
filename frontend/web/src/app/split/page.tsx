'use client';

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { FileUpload } from '@/components/file-upload';
import { filesApi, pdfApi } from '@/lib/api-client';
import { useAuthStore } from '@/store/auth-store';
import toast from 'react-hot-toast';
import { Loader2, Scissors, ArrowRight, Download } from 'lucide-react';

export default function SplitPage() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const [files, setFiles] = useState<File[]>([]);
  const [pageRanges, setPageRanges] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [resultFileId, setResultFileId] = useState<string | null>(null);

  const handleFilesSelected = useCallback((selectedFiles: File[]) => {
    setFiles(selectedFiles.slice(0, 1));
    setResultFileId(null);
  }, []);

  const handleSplit = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to continue');
      router.push('/login');
      return;
    }

    if (files.length === 0) {
      toast.error('Please select a PDF file');
      return;
    }

    if (!pageRanges.trim()) {
      toast.error('Please specify page ranges (e.g., 1-3,5,7-10)');
      return;
    }

    try {
      // Step 1: Upload file
      setIsUploading(true);
      const uploadResult = await filesApi.upload(files[0], true);
      const fileId = uploadResult.data.fileId;
      toast.success('File uploaded');

      // Step 2: Start split job
      setIsUploading(false);
      setIsProcessing(true);

      const splitResult = await pdfApi.split(fileId, pageRanges);

      // Step 3: Poll for completion
      let status = splitResult.data.status;
      let attempts = 0;
      const maxAttempts = 60;

      while (status !== 'COMPLETED' && status !== 'FAILED' && attempts < maxAttempts) {
        await new Promise((resolve) => setTimeout(resolve, 5000));
        const statusResult = await pdfApi.getJobStatus(splitResult.data.jobId);
        status = statusResult.data.status;

        if (statusResult.data.resultFileId) {
          setResultFileId(statusResult.data.resultFileId);
        }

        if (statusResult.data.errorMessage) {
          throw new Error(statusResult.data.errorMessage);
        }

        attempts++;
      }

      if (status === 'COMPLETED') {
        toast.success('PDF split successfully!');
      } else if (status === 'FAILED') {
        toast.error('Split failed');
      }
    } catch (error: any) {
      toast.error(error.message || 'Failed to split PDF');
    } finally {
      setIsUploading(false);
      setIsProcessing(false);
    }
  };

  const handleDownload = async () => {
    if (!resultFileId) return;

    try {
      const blob = await filesApi.download(resultFileId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'split.pdf';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success('Download started');
    } catch (error) {
      toast.error('Failed to download file');
    }
  };

  return (
    <>
      <Navbar />
      <main className="min-h-screen bg-gray-50 py-12">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="text-center mb-12">
            <div className="bg-green-500 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Scissors className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Split PDF</h1>
            <p className="text-lg text-gray-600">
              Extract specific pages from your PDF document
            </p>
          </div>

          {/* Upload Section */}
          <div className="card mb-6">
            <FileUpload
              onFilesSelected={handleFilesSelected}
              maxFiles={1}
              multiple={false}
            />
          </div>

          {/* Page Ranges */}
          <div className="card mb-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Page Ranges
            </h3>
            <p className="text-sm text-gray-600 mb-4">
              Specify which pages to extract. Examples: &quot;1-3&quot; for pages 1 to 3, 
              &quot;1,3,5&quot; for specific pages, or &quot;1-3,5,7-10&quot; for combinations.
            </p>
            <input
              type="text"
              value={pageRanges}
              onChange={(e) => setPageRanges(e.target.value)}
              placeholder="e.g., 1-3,5,7-10"
              className="input"
            />
          </div>

          {/* Action Buttons */}
          <div className="flex justify-center gap-4">
            {!resultFileId ? (
              <button
                onClick={handleSplit}
                disabled={files.length === 0 || !pageRanges.trim() || isUploading || isProcessing}
                className="btn-primary flex items-center gap-2 text-lg px-8 py-3"
              >
                {(isUploading || isProcessing) && (
                  <Loader2 className="w-5 h-5 animate-spin" />
                )}
                {isUploading
                  ? 'Uploading...'
                  : isProcessing
                  ? 'Processing...'
                  : 'Split PDF'}
                {!isUploading && !isProcessing && (
                  <ArrowRight className="w-5 h-5" />
                )}
              </button>
            ) : (
              <button
                onClick={handleDownload}
                className="btn-primary flex items-center gap-2 text-lg px-8 py-3 bg-green-600 hover:bg-green-700"
              >
                <Download className="w-5 h-5" />
                Download Split PDF
              </button>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
