'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { useAuthStore } from '@/store/auth-store';
import { pdfApi, filesApi } from '@/lib/api-client';
import { FileText, Clock, CheckCircle, XCircle, HardDrive } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';

export default function DashboardPage() {
  const { isAuthenticated, user } = useAuthStore();
  const router = useRouter();
  const [jobs, setJobs] = useState<any[]>([]);
  const [storageUsage, setStorageUsage] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    loadDashboardData();
  }, [isAuthenticated, router]);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);
      const [jobsResult, usageResult] = await Promise.all([
        pdfApi.getMyJobs(0, 10),
        filesApi.getUsage(),
      ]);

      setJobs(jobsResult.data.content);
      setStorageUsage(usageResult.data);
    } catch (error) {
      toast.error('Failed to load dashboard data');
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <Clock className="w-5 h-5 text-yellow-500" />;
    }
  };

  const getJobTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      MERGE: 'Merge',
      SPLIT: 'Split',
      COMPRESS: 'Compress',
      WATERMARK: 'Watermark',
      PROTECT: 'Protect',
      UNLOCK: 'Unlock',
      ROTATE: 'Rotate',
    };
    return labels[type] || type;
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <>
      <Navbar />
      <main className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">
              Welcome, {user?.firstName}!
            </h1>
            <p className="text-gray-600 mt-1">
              Manage your PDF processing jobs and files
            </p>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="card">
              <div className="flex items-center gap-4">
                <div className="bg-primary-100 p-3 rounded-lg">
                  <FileText className="w-6 h-6 text-primary-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">Total Jobs</p>
                  <p className="text-2xl font-bold text-gray-900">{jobs.length}</p>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="flex items-center gap-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <CheckCircle className="w-6 h-6 text-green-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">Completed</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {jobs.filter((j) => j.status === 'COMPLETED').length}
                  </p>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="flex items-center gap-4">
                <div className="bg-purple-100 p-3 rounded-lg">
                  <HardDrive className="w-6 h-6 text-purple-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">Storage Used</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {storageUsage?.formattedUsed || '0 B'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Recent Jobs */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Recent Jobs
            </h2>

            {isLoading ? (
              <div className="text-center py-8 text-gray-500">Loading...</div>
            ) : jobs.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <FileText className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                <p>No jobs yet. Start by processing a PDF!</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="text-left border-b border-gray-200">
                      <th className="pb-3 font-medium text-gray-600">Type</th>
                      <th className="pb-3 font-medium text-gray-600">Status</th>
                      <th className="pb-3 font-medium text-gray-600">Created</th>
                      <th className="pb-3 font-medium text-gray-600">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jobs.map((job) => (
                      <tr key={job.id} className="border-b border-gray-100">
                        <td className="py-4">
                          <span className="font-medium text-gray-900">
                            {getJobTypeLabel(job.type)}
                          </span>
                        </td>
                        <td className="py-4">
                          <div className="flex items-center gap-2">
                            {getStatusIcon(job.status)}
                            <span className="text-gray-700">{job.status}</span>
                          </div>
                        </td>
                        <td className="py-4 text-gray-600">
                          {format(new Date(job.createdAt), 'MMM d, yyyy HH:mm')}
                        </td>
                        <td className="py-4">
                          {job.status === 'COMPLETED' && (
                            <button className="text-primary-600 hover:text-primary-700 font-medium">
                              Download
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
