import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/store/auth-store';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const { accessToken } = useAuthStore.getState();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const { refreshToken, updateTokens, logout } = useAuthStore.getState();

      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/api/v1/auth/refresh`, {
            refreshToken,
          });

          const { accessToken: newAccessToken, refreshToken: newRefreshToken } =
            response.data.data;

          updateTokens(newAccessToken, newRefreshToken);

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return apiClient(originalRequest);
        } catch (refreshError) {
          logout();
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      } else {
        logout();
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

// API Response type
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

// Auth API
export const authApi = {
  login: async (email: string, password: string) => {
    const response = await apiClient.post<ApiResponse<{
      accessToken: string;
      refreshToken: string;
      expiresIn: number;
      user: {
        id: string;
        email: string;
        firstName: string;
        lastName: string;
        roles: string[];
      };
    }>>('/api/v1/auth/login', { email, password });
    return response.data;
  },

  register: async (data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => {
    const response = await apiClient.post<ApiResponse<{
      accessToken: string;
      refreshToken: string;
      user: {
        id: string;
        email: string;
        firstName: string;
        lastName: string;
        roles: string[];
      };
    }>>('/api/v1/auth/register', data);
    return response.data;
  },

  logout: async (refreshToken: string) => {
    const response = await apiClient.post<ApiResponse<void>>('/api/v1/auth/logout', {
      refreshToken,
    });
    return response.data;
  },
};

// PDF API
export const pdfApi = {
  merge: async (fileIds: string[], outputFileName?: string) => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/merge', { fileIds, outputFileName });
    return response.data;
  },

  compress: async (fileId: string, level: 'LOW' | 'MEDIUM' | 'HIGH') => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/compress', { fileId, compressionLevel: level });
    return response.data;
  },

  split: async (fileId: string, pageRanges: string) => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/split', { fileId, pageRanges });
    return response.data;
  },

  watermark: async (fileId: string, watermarkText: string, position: string) => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/watermark', { fileId, watermarkText, position });
    return response.data;
  },

  protect: async (fileId: string, userPassword: string, ownerPassword: string) => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/protect', { fileId, userPassword, ownerPassword });
    return response.data;
  },

  unlock: async (fileId: string, password: string) => {
    const response = await apiClient.post<ApiResponse<{
      jobId: string;
      status: string;
    }>>('/api/v1/pdf/unlock', { fileId, password });
    return response.data;
  },

  getJobStatus: async (jobId: string) => {
    const response = await apiClient.get<ApiResponse<{
      id: string;
      status: string;
      progress: number;
      resultFileId?: string;
      errorMessage?: string;
    }>>(`/api/v1/pdf/jobs/${jobId}`);
    return response.data;
  },

  getMyJobs: async (page: number = 0, size: number = 10) => {
    const response = await apiClient.get<ApiResponse<{
      content: Array<{
        id: string;
        type: string;
        status: string;
        createdAt: string;
      }>;
      totalElements: number;
      totalPages: number;
    }>>(`/api/v1/pdf/jobs?page=${page}&size=${size}`);
    return response.data;
  },
};

// Files API
export const filesApi = {
  upload: async (file: File, temporary: boolean = false) => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post<ApiResponse<{
      fileId: string;
      fileName: string;
      contentType: string;
      sizeBytes: number;
      downloadUrl: string;
    }>>(`/api/v1/files?temporary=${temporary}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 300000, // 5 minutes for large files
    });
    return response.data;
  },

  getInfo: async (fileId: string) => {
    const response = await apiClient.get<ApiResponse<{
      fileId: string;
      fileName: string;
      contentType: string;
      sizeBytes: number;
      downloadUrl: string;
    }>>(`/api/v1/files/${fileId}/metadata`);
    return response.data;
  },

  download: async (fileId: string) => {
    const response = await apiClient.get(`/api/v1/files/${fileId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  delete: async (fileId: string) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/api/v1/files/${fileId}`);
    return response.data;
  },

  list: async (page: number = 0, size: number = 20) => {
    const response = await apiClient.get<ApiResponse<{
      files: Array<{
        fileId: string;
        fileName: string;
        contentType: string;
        sizeBytes: number;
        createdAt: string;
      }>;
      totalElements: number;
      totalPages: number;
    }>>(`/api/v1/files?page=${page}&size=${size}`);
    return response.data;
  },

  getUsage: async () => {
    const response = await apiClient.get<ApiResponse<{
      totalBytes: number;
      usedBytes: number;
      availableBytes: number;
      fileCount: number;
      formattedUsed: string;
      formattedTotal: string;
    }>>('/api/v1/files/usage');
    return response.data;
  },
};
