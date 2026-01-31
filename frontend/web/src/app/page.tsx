import Link from 'next/link';
import { FileText, Merge, Scissors, Minimize2, Shield, Lock } from 'lucide-react';

export default function HomePage() {
  const features = [
    {
      icon: Merge,
      title: 'Merge PDFs',
      description: 'Combine multiple PDF files into a single document',
      href: '/merge',
      color: 'bg-blue-500',
    },
    {
      icon: Scissors,
      title: 'Split PDF',
      description: 'Extract pages or split PDF into multiple files',
      href: '/split',
      color: 'bg-green-500',
    },
    {
      icon: Minimize2,
      title: 'Compress PDF',
      description: 'Reduce file size while maintaining quality',
      href: '/compress',
      color: 'bg-purple-500',
    },
    {
      icon: FileText,
      title: 'Add Watermark',
      description: 'Add text or image watermarks to PDF pages',
      href: '/watermark',
      color: 'bg-orange-500',
    },
    {
      icon: Shield,
      title: 'Protect PDF',
      description: 'Add password protection to your PDFs',
      href: '/protect',
      color: 'bg-red-500',
    },
    {
      icon: Lock,
      title: 'Unlock PDF',
      description: 'Remove password from protected PDFs',
      href: '/unlock',
      color: 'bg-teal-500',
    },
  ];

  return (
    <main className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-primary-600 to-primary-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              PDF Utility Platform
            </h1>
            <p className="text-xl md:text-2xl text-primary-100 mb-8 max-w-3xl mx-auto">
              Enterprise-grade PDF processing tools. Merge, split, compress, 
              and protect your documents with ease.
            </p>
            <div className="flex gap-4 justify-center">
              <Link href="/login" className="btn-primary bg-white text-primary-700 hover:bg-gray-100">
                Get Started
              </Link>
              <Link href="#features" className="btn-secondary bg-primary-700 text-white hover:bg-primary-600">
                Learn More
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-24 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Powerful PDF Tools
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Everything you need to work with PDF documents, 
              all in one secure platform.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature) => (
              <Link 
                key={feature.title} 
                href={feature.href}
                className="card hover:shadow-lg transition-shadow group"
              >
                <div className={`${feature.color} w-12 h-12 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                  <feature.icon className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600">
                  {feature.description}
                </p>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p>&copy; 2024 PDF Utility Platform. All rights reserved.</p>
        </div>
      </footer>
    </main>
  );
}
