'use client';

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { FileUpload } from '@/components/file-upload';
import { filesApi, pdfApi } from '@/lib/api-client';
import { useAuthStore } from '@/store/auth-store';
import toast from 'react-hot-toast';
import { Loader2, Minimize2, ArrowRight, Download } from 'lucide-react';

type CompressionLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export default function CompressPage() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const [files, setFiles] = useState<File[]>([]);
  const [compressionLevel, setCompressionLevel] = useState<CompressionLevel>('MEDIUM');
  const [isUploading, setIsUploading] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [resultFileId, setResultFileId] = useState<string | null>(null);

  const handleFilesSelected = useCallback((selectedFiles: File[]) => {
    setFiles(selectedFiles.slice(0, 1)); // Single file for compression
    setResultFileId(null);
  }, []);

  const handleCompress = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to continue');
      router.push('/login');
      return;
    }

    if (files.length === 0) {
      toast.error('Please select a PDF file to compress');
      return;
    }

    try {
      // Step 1: Upload file
      setIsUploading(true);
      const uploadResult = await filesApi.upload(files[0], true);
      const fileId = uploadResult.data.fileId;
      toast.success('File uploaded');

      // Step 2: Start compress job
      setIsUploading(false);
      setIsProcessing(true);

      const compressResult = await pdfApi.compress(fileId, compressionLevel);

      // Step 3: Poll for completion
      let status = compressResult.data.status;
      let attempts = 0;
      const maxAttempts = 60;

      while (status !== 'COMPLETED' && status !== 'FAILED' && attempts < maxAttempts) {
        await new Promise((resolve) => setTimeout(resolve, 5000));
        const statusResult = await pdfApi.getJobStatus(compressResult.data.jobId);
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
        toast.success('PDF compressed successfully!');
      } else if (status === 'FAILED') {
        toast.error('Compression failed');
      }
    } catch (error: any) {
      toast.error(error.message || 'Failed to compress PDF');
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
      a.download = 'compressed.pdf';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success('Download started');
    } catch (error) {
      toast.error('Failed to download file');
    }
  };

  const compressionOptions = [
    { level: 'LOW' as const, label: 'Low', description: 'Best quality, smaller reduction' },
    { level: 'MEDIUM' as const, label: 'Medium', description: 'Balanced quality and size' },
    { level: 'HIGH' as const, label: 'High', description: 'Smallest size, lower quality' },
  ];

  return (
    <>
      <Navbar />
      <main className="min-h-screen bg-gray-50 py-12">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="text-center mb-12">
            <div className="bg-purple-500 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Minimize2 className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Compress PDF</h1>
            <p className="text-lg text-gray-600">
              Reduce file size while maintaining quality
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

          {/* Compression Level */}
          <div className="card mb-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Compression Level
            </h3>
            <div className="grid grid-cols-3 gap-4">
              {compressionOptions.map((option) => (
                <button
                  key={option.level}
                  onClick={() => setCompressionLevel(option.level)}
                  className={`p-4 rounded-lg border-2 transition-all text-left ${
                    compressionLevel === option.level
                      ? 'border-primary-500 bg-primary-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="font-semibold text-gray-900">{option.label}</div>
                  <div className="text-sm text-gray-600">{option.description}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-center gap-4">
            {!resultFileId ? (
              <button
                onClick={handleCompress}
                disabled={files.length === 0 || isUploading || isProcessing}
                className="btn-primary flex items-center gap-2 text-lg px-8 py-3"
              >
                {(isUploading || isProcessing) && (
                  <Loader2 className="w-5 h-5 animate-spin" />
                )}
                {isUploading
                  ? 'Uploading...'
                  : isProcessing
                  ? 'Compressing...'
                  : 'Compress PDF'}
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
                Download Compressed PDF
              </button>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
