'use client';

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { FileUpload } from '@/components/file-upload';
import { filesApi, pdfApi } from '@/lib/api-client';
import { useAuthStore } from '@/store/auth-store';
import toast from 'react-hot-toast';
import { Loader2, Merge, ArrowRight, Download } from 'lucide-react';

export default function MergePage() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const [files, setFiles] = useState<File[]>([]);
  const [uploadedFileIds, setUploadedFileIds] = useState<string[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [jobId, setJobId] = useState<string | null>(null);
  const [resultFileId, setResultFileId] = useState<string | null>(null);

  const handleFilesSelected = useCallback((selectedFiles: File[]) => {
    setFiles(selectedFiles);
    setUploadedFileIds([]);
    setJobId(null);
    setResultFileId(null);
  }, []);

  const handleMerge = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to continue');
      router.push('/login');
      return;
    }

    if (files.length < 2) {
      toast.error('Please select at least 2 PDF files to merge');
      return;
    }

    try {
      // Step 1: Upload files
      setIsUploading(true);
      const uploadPromises = files.map((file) => filesApi.upload(file, true));
      const uploadResults = await Promise.all(uploadPromises);
      
      const fileIds = uploadResults.map((result) => result.data.fileId);
      setUploadedFileIds(fileIds);
      toast.success(`${files.length} files uploaded`);

      // Step 2: Start merge job
      setIsUploading(false);
      setIsProcessing(true);
      
      const mergeResult = await pdfApi.merge(fileIds, 'merged.pdf');
      setJobId(mergeResult.data.jobId);

      // Step 3: Poll for completion
      let status = mergeResult.data.status;
      let attempts = 0;
      const maxAttempts = 60; // 5 minutes max

      while (status !== 'COMPLETED' && status !== 'FAILED' && attempts < maxAttempts) {
        await new Promise((resolve) => setTimeout(resolve, 5000)); // Wait 5 seconds
        const statusResult = await pdfApi.getJobStatus(mergeResult.data.jobId);
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
        toast.success('PDFs merged successfully!');
      } else if (status === 'FAILED') {
        toast.error('Merge failed');
      } else {
        toast.error('Merge timed out');
      }
    } catch (error: any) {
      toast.error(error.message || 'Failed to merge PDFs');
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
      a.download = 'merged.pdf';
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
            <div className="bg-blue-500 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Merge className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Merge PDFs</h1>
            <p className="text-lg text-gray-600">
              Combine multiple PDF files into a single document
            </p>
          </div>

          {/* Upload Section */}
          <div className="card mb-8">
            <FileUpload
              onFilesSelected={handleFilesSelected}
              maxFiles={20}
              multiple={true}
            />
          </div>

          {/* Action Buttons */}
          <div className="flex justify-center gap-4">
            {!resultFileId ? (
              <button
                onClick={handleMerge}
                disabled={files.length < 2 || isUploading || isProcessing}
                className="btn-primary flex items-center gap-2 text-lg px-8 py-3"
              >
                {(isUploading || isProcessing) && (
                  <Loader2 className="w-5 h-5 animate-spin" />
                )}
                {isUploading
                  ? 'Uploading...'
                  : isProcessing
                  ? 'Processing...'
                  : 'Merge PDFs'}
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
                Download Merged PDF
              </button>
            )}
          </div>

          {/* Info Section */}
          <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="card text-center">
              <div className="text-3xl font-bold text-primary-600 mb-2">Fast</div>
              <p className="text-gray-600">Merge PDFs in seconds</p>
            </div>
            <div className="card text-center">
              <div className="text-3xl font-bold text-primary-600 mb-2">Secure</div>
              <p className="text-gray-600">Files are encrypted and auto-deleted</p>
            </div>
            <div className="card text-center">
              <div className="text-3xl font-bold text-primary-600 mb-2">Free</div>
              <p className="text-gray-600">No watermarks or limits</p>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
