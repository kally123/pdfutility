'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { FileText, User, LogOut, Menu, X } from 'lucide-react';
import { useState } from 'react';
import { useAuthStore } from '@/store/auth-store';
import { authApi } from '@/lib/api-client';
import toast from 'react-hot-toast';

export function Navbar() {
  const { isAuthenticated, user, logout, refreshToken } = useAuthStore();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const router = useRouter();

  const handleLogout = async () => {
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (error) {
      // Ignore logout errors
    } finally {
      logout();
      toast.success('Logged out successfully');
      router.push('/');
    }
  };

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="flex items-center gap-2">
              <FileText className="w-8 h-8 text-primary-600" />
              <span className="text-xl font-bold text-gray-900">PDF Utility</span>
            </Link>
          </div>

          {/* Desktop Menu */}
          <div className="hidden md:flex items-center gap-6">
            <Link href="/merge" className="text-gray-600 hover:text-primary-600 transition-colors">
              Merge
            </Link>
            <Link href="/split" className="text-gray-600 hover:text-primary-600 transition-colors">
              Split
            </Link>
            <Link href="/compress" className="text-gray-600 hover:text-primary-600 transition-colors">
              Compress
            </Link>

            {isAuthenticated ? (
              <div className="flex items-center gap-4">
                <Link href="/dashboard" className="flex items-center gap-2 text-gray-600 hover:text-primary-600">
                  <User className="w-5 h-5" />
                  <span>{user?.firstName}</span>
                </Link>
                <button onClick={handleLogout} className="flex items-center gap-2 text-gray-600 hover:text-red-600">
                  <LogOut className="w-5 h-5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-4">
                <Link href="/login" className="text-gray-600 hover:text-primary-600">
                  Login
                </Link>
                <Link href="/register" className="btn-primary">
                  Get Started
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="p-2"
            >
              {isMobileMenuOpen ? (
                <X className="w-6 h-6" />
              ) : (
                <Menu className="w-6 h-6" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden bg-white border-t">
          <div className="px-4 py-4 space-y-3">
            <Link href="/merge" className="block text-gray-600 hover:text-primary-600">
              Merge
            </Link>
            <Link href="/split" className="block text-gray-600 hover:text-primary-600">
              Split
            </Link>
            <Link href="/compress" className="block text-gray-600 hover:text-primary-600">
              Compress
            </Link>
            <hr />
            {isAuthenticated ? (
              <>
                <Link href="/dashboard" className="block text-gray-600 hover:text-primary-600">
                  Dashboard
                </Link>
                <button onClick={handleLogout} className="block text-red-600">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="block text-gray-600">
                  Login
                </Link>
                <Link href="/register" className="block btn-primary text-center">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
